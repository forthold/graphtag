//<script language="text/javascript">
//	alert("asdfdsaf");
//</script>
//
$(document).ready(function() {
var width = 960,
    height = 500,
    node,
    link,
    root;

    //.charge(function(d) { return d._children ? -d.size / 100 : -30; })
var force = d3.layout.force()
    .on("tick", tick)
    .gravity(.05)
    .charge(function(d) { return d._children ? -100 : -300; })
    .linkDistance(function(d) { return d.target._children ? 80 : 30; })
    .size([width, height]);

var vis = d3.select("#chart").append("svg")
    .attr("width", width)
    .attr("height", height);

//d3.json("../data/flare.json", function(json) {
//d3.json("/forthold.json", function(json) {
//d3.json("forthold3.json", function(json) {
  root = json;
  root.fixed = true;
  root.x = width / 2;
  root.y = height / 2;
  update();
  console.log('json:', json);
  console.log('root:', root);

function update() {
  var nodes = flatten(root),
      links = d3.layout.tree().links(nodes);
      console.log("nodes:" , nodes);
      console.log("links:" , links);

  // Restart the force layout.
  force.nodes(nodes)
      .links(links)
      .start();

  // Update the links…
  link = vis.selectAll("line.link")
      .data(links, function(d) { return d.target.id; });

  // Enter any new links.
  link.enter().insert("line", ".node")
      .attr("class", "link")
      .attr("x1", function(d) { return d.source.x; })
      .attr("y1", function(d) { return d.source.y; })
      .attr("x2", function(d) { return d.target.x; })
      .attr("y2", function(d) { return d.target.y; });

  // Exit any old links.
  link.exit().remove();

  // Update the nodes…
  node = vis.selectAll("circle.node")
      .data(nodes, function(d) { return d.id; })
      .style("fill", color);

  console.log("vis.selectAll circle.node ", vis.selectAll("circle.node"));

  node.transition()
      .attr("r", function(d) { return 10; });
      //.attr("r", function(d) { return d.children ? 4.5 : Math.sqrt(d.size) / 10; });

  // Enter any new nodes.
  node.enter().append("circle")
      .attr("class", "node")
      .attr("cx", function(d) { return d.x; })
      .attr("cy", function(d) { return d.y; })
      //.attr("r", function(d) { return d.children ? 4.5 : Math.sqrt(d.size) / 10; })
      .attr("r", function(d) { return 10;})
      .style("fill", color)
      .on("click", click)
      .call(force.drag);

  node.append("text")
	.attr("text-anchor", "middle") 
	.attr("fill","white")
	.attr("font-size", function(d) { return "9px";})
	.attr("font-weight", function(d) { return "100"; })
	.text("asdfadfadsf" ) ;
	//.text( function(d) { if (d.color == '#b94431') { return d.id + ' (' + d.size + ')';} else { return d.id;} } ) ;

  // Exit any old nodes.
  node.exit().remove();
}

function tick() {
  link.attr("x1", function(d) { return d.source.x; })
      .attr("y1", function(d) { return d.source.y; })
      .attr("x2", function(d) { return d.target.x; })
      .attr("y2", function(d) { return d.target.y; });

  node.attr("cx", function(d) { return d.x; })
      .attr("cy", function(d) { return d.y; });
}

// Color leaf nodes orange, and packages white or blue.
function color(d) {
  return d._children ? "#3182bd" : d.children ? "#c6dbef" : "#fd8d3c";
}

// Toggle children on click.
function click(d) {
  if (d.children) {
    d._children = d.children;
    d.children = null;
  } else {
    d.children = d._children;
    d._children = null;
  }
  update();
}

// Returns a list of all nodes under the root.
function flatten(root) {
  var nodes = [], i = 0;

  function recurse(node) {
    if (node.children) node.size = node.children.reduce(function(p, v) { return p + recurse(v); }, 0);
    if (!node.id) node.id = ++i;
    nodes.push(node);
    return node.size;
  }

  root.size = recurse(root);
  return nodes;
}
});
