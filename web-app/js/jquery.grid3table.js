(function ( $ ) {
  $.fn.grid3table = function() {
    this.each(function(i, node) {
      new Grid3Widget(node);
    });
    return this;
  };

  function Grid3Widget(container) {
    this.container = $(container);
    this.container.addClass('grid3table-container');
    this.query = this.container.attr('data-query');
    this.queryXml = $($.parseXML(this.query));
    this.generateTable();
    this.url = this.container.attr('data-url');
    this.selectedCell = null;
    this.editSelectedCell = false;
    this.loadData();
  }
  Grid3Widget.prototype.generateTable = function() {
    this.table = $('<table><thead></thead><tbody></tbody></table>');
    var headerRow = $('<tr>');
    this.table.find('thead').append(headerRow);
    var viewColumnPathes = this.viewColumnPathes = new Array();
    var viewColumnUpdateModes = this.viewColumnUpdateModes = new Array();
    this.queryXml.find("> hqlXml > columns > column").each(function(){
      var path = $(this).attr('path');
      headerRow.append($('<td>').text(path));
      viewColumnPathes.push(path);
      viewColumnUpdateModes.push($(this).attr('editable'));
    });

    this.spinnerRow = $('<tr class="grid3-spinner">');
    var spinnerTd = $('<td>');
    spinnerTd.attr('colspan', this.queryXml.find("column").length);
    spinnerTd.text('Loading...');
    this.spinnerRow.append(spinnerTd);
    this.table.find('tbody').append(this.spinnerRow);

    this.container.append(this.table);
    var grid3widget = this;
    this.table.mousedown(function(event) {
      var cell = grid3widget.findCellForEvent(event);
      grid3widget.selectCell(cell);
    });
    $(document).keydown(function(event) {
      if(grid3widget.selectedCell){
        if(event.keyCode == 13) {// ENTER key is pressed
          if(grid3widget.editSelectedCell){
            grid3widget.makeSelectedCellNotEditable();
            // TODO: save changes
          }
          else{
            grid3widget.makeSelectedCellEditable();
          }
        }
      }
    });
  };
  Grid3Widget.prototype.loadData = function() {
    $.ajax({url: this.url + '/index', data: {'query' : this.query}, dataType: 'json', context: this}).done(this.loadDataCallback);
  };
  Grid3Widget.prototype.loadDataCallback = function(data){
    this.model = data;
    var columnByPath = [];
    var columns = this.model.columns;
    for (var i = 0; i < columns.length; i++) {
      var column = columns[i];
      columnByPath[column.path] = i;
    }
    this.columnByPath = columnByPath;
    this.buildRowTable();
    this.spinnerRow.hide();
  };
  Grid3Widget.prototype.buildRowTable = function(){
    for (var i = 0; i < this.model.rows.length; i++) {
      var row = this.model.rows[i];
      var tr = $('<tr>');
      tr.data('originalRow', row);
      this.table.find('tbody').append(tr);
      for (var j = 0; j < this.viewColumnPathes.length; j++) {
        var columnPath = this.viewColumnPathes[j];
        var index = this.columnByPath[columnPath];
        var value = row[index];

        var td = $('<td>').text(value);
        var updateMode = this.viewColumnUpdateModes[j];
        if(updateMode){
          td.addClass('update-' + updateMode);
        }
        else{
          td.addClass('readonly');
        }
        tr.append(td);
      }
    }
  };
  Grid3Widget.prototype.findCellForEvent = function(event) {
    var eventTarget = $(event.target);
    var res = eventTarget.closest('td');
    return res;
  };
  Grid3Widget.prototype.selectCell = function(cell) {
    if(cell.is(this.selectedCell)) {
      return;
    }
    this.makeSelectedCellNotEditable();
    this.removeSelection(this.selectedCell);
    this.selectedCell = cell;
    this.addSelection(cell);
  };
  Grid3Widget.prototype.removeSelection = function(cell) {
    if(cell) {
      cell.removeClass('selected');
    }
  };
  Grid3Widget.prototype.addSelection = function(cell) {
    cell.addClass('selected');
  };
  Grid3Widget.prototype.makeSelectedCellEditable = function() {

    if(this.selectedCell.hasClass('update-value')){
      this.renderValueCellEditor(this.selectedCell);
    }
    else if(this.selectedCell.hasClass('update-reference')){
      this.renderReferenceCellEditor(this.selectedCell);
    }
    else{
      return;
    }
    this.editSelectedCell = true;
  };
  Grid3Widget.prototype.makeSelectedCellNotEditable = function() {
    if(!this.editSelectedCell)
      return;
    if(this.selectedCell.hasClass('update-value')){
      this.removeValueCellEditor(this.selectedCell);
      this.updateValueCellRemotely(this.selectedCell);
    }
    else if(this.selectedCell.hasClass('update-reference')){
      var newReference = this.removeReferenceCellEditor(this.selectedCell);
      this.updateReferenceCellRemotely(this.selectedCell, newReference);
    }
    this.editSelectedCell = false;
  };
  Grid3Widget.prototype.renderValueCellEditor = function(cell) {
    var text = $(cell).text();
    cell.text('');
    var input = $('<input>').val(text);
    cell.append(input);
    input.focus();
  }
  Grid3Widget.prototype.removeValueCellEditor = function(cell) {
    var text = $(cell).find('input').remove().val();
    $(cell).text(text);
  }
  Grid3Widget.prototype.renderReferenceCellEditor = function(cell) {
    var data = this.buildQueryJson(cell, this.getCellIndex(cell));
    $.ajax(
        {
          type: 'GET',
          url: this.url + '/referenceOptions',
          data: data,
          dataType: 'json',
          context: this
        }
    ).done($.proxy(this.renderReferenceCellEditorCallback, cell));
  }

  Grid3Widget.prototype.renderReferenceCellEditorCallback = function(data) {
    var cell = this;
    var text = $(cell).text();
    cell.text('');
    var select = $('<select>');
    var rows = data.rows;
    for (var i = 0; i < rows.length; i++) {
      var row = rows[i];
      var option = $('<option>');
      option.attr('value', row[0]);
      option.text(row[1]);
      select.append(option);
    }
    cell.append(select);
    select.focus();
  }
  Grid3Widget.prototype.removeReferenceCellEditor = function(cell) {
    var res = $(cell).find('select').val();
    var text = $(cell).find('select option:selected').remove().text();
    $(cell).text(text);
    return res;
  }
  Grid3Widget.prototype.updateValueCellRemotely = function(cell) {
    var data = this.buildQueryJson(cell, this.getCellIndex(cell));
    data.value = cell.text();
    $.ajax(
      {
        type: 'POST',
        url: this.url + '/update',
        data: data,
        dataType: 'json',
        context: this
      }
    ).done(this.updateCellRemotelyCallback);
  };
  Grid3Widget.prototype.buildQueryJson = function(cell, index){
    var originalRow = $(cell).closest('tr').data('originalRow');
    return {'query' : this.query, 'originalRow' : JSON.stringify(originalRow), 'path' : this.viewColumnPathes[index]};
  }
  Grid3Widget.prototype.updateReferenceCellRemotely = function(cell, newReference) {
    var valueViewIndex = this.getCellIndex(cell);
    var valuePath = this.viewColumnPathes[valueViewIndex];
    var valueColumnIndex = this.columnByPath[valuePath];
    var valueColumn = this.model.columns[valueColumnIndex];
    alert(valueColumn);

    var originalRow = $(cell).closest('tr').data('originalRow');
    var data = {'query' : this.query, 'originalRow' : JSON.stringify(originalRow), 'path' : valueColumn.target};

    data.value = newReference;
    $.ajax(
        {
          type: 'POST',
          url: this.url + '/update',
          data: data,
          dataType: 'json',
          context: this
        }
    ).done(this.updateCellRemotelyCallback);
  }

  Grid3Widget.prototype.updateCellRemotelyCallback = function(cell) {

  }
  Grid3Widget.prototype.getCellIndex = function(cell){
    return $(cell).index();
  }
}( jQuery ));