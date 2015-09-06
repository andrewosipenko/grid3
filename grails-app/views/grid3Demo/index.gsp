hello2
<table>
  <thead>
    <tr>
      <g:each in="${grid3View.grid3.grid3Columns}" var="grid3Column">
        <td>${grid3Column.path}</td>
      </g:each>
      <td>Actions</td>
    </tr>
  </thead>
  <tbody>
    <g:each in="${grid3View.rows}" var="grid3Row">
      <tr>
        <g:each in="${grid3View.grid3.grid3Columns}" var="grid3Column">
          <td>${grid3Column.getValue(grid3Row)}</td>
        </g:each>
        <td><a href="#">Delete</a></td>
      </tr>
    </g:each>
  </tbody>
</table>