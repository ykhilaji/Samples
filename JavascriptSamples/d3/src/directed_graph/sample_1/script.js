// https://tomroth.com.au/d3/
// https://tomroth.com.au/fdg-basics/

window.onload = function () {
    function tickActions(node, link) {
        // constrains the nodes to be within a box
        // node
        //     .attr("cx", function(d) { return d.x = Math.max(radius, Math.min(width - radius, d.x)); })
        //     .attr("cy", function(d) { return d.y = Math.max(radius, Math.min(height - radius, d.y)); });

        node
            .attr("cx", d => d.x)
            .attr("cy", d => d.y);

        link
            .attr("x1", d => d.source.x)
            .attr("y1", d => d.source.y)
            .attr("x2", d => d.target.x)
            .attr("y2", d => d.target.y);
    }

    function dragStart(d, simulation) {
        if (!d3.event.active) simulation.alphaTarget(0.3).restart();
        d.fx = d.x;
        d.fy = d.y;
    }

    function dragDrag(d) {
        d.fx = d3.event.x;
        d.fy = d3.event.y;
    }

    function dragEnd(d, simulation) {
        if (!d3.event.active) simulation.alphaTarget(0);
        d.fx = null;
        d.fy = null;
    }

    const nodes_data =  [
        {"name": "Lillian", "sex": "F"},
        {"name": "Gordon", "sex": "M"},
        {"name": "Sylvester", "sex": "M"},
        {"name": "Mary", "sex": "F"},
        {"name": "Helen", "sex": "F"},
        {"name": "Jamie", "sex": "M"},
        {"name": "Jessie", "sex": "F"},
        {"name": "Ashton", "sex": "M"},
        {"name": "Duncan", "sex": "M"},
        {"name": "Evette", "sex": "F"},
        {"name": "Mauer", "sex": "M"},
        {"name": "Fray", "sex": "F"},
        {"name": "Duke", "sex": "M"},
        {"name": "Baron", "sex": "M"},
        {"name": "Infante", "sex": "M"},
        {"name": "Percy", "sex": "M"},
        {"name": "Cynthia", "sex": "F"}
    ];

    const links_data = [
        {"source": "Sylvester", "target": "Gordon", "type":"A" },
        {"source": "Sylvester", "target": "Lillian", "type":"A" },
        {"source": "Sylvester", "target": "Mary", "type":"A"},
        {"source": "Sylvester", "target": "Jamie", "type":"A"},
        {"source": "Sylvester", "target": "Jessie", "type":"A"},
        {"source": "Sylvester", "target": "Helen", "type":"A"},
        {"source": "Helen", "target": "Gordon", "type":"A"},
        {"source": "Mary", "target": "Lillian", "type":"A"},
        {"source": "Ashton", "target": "Mary", "type":"A"},
        {"source": "Duncan", "target": "Jamie", "type":"A"},
        {"source": "Gordon", "target": "Jessie", "type":"A"},
        {"source": "Sylvester", "target": "Fray", "type":"E"},
        {"source": "Fray", "target": "Mauer", "type":"A"},
        {"source": "Fray", "target": "Cynthia", "type":"A"},
        {"source": "Fray", "target": "Percy", "type":"A"},
        {"source": "Percy", "target": "Cynthia", "type":"A"},
        {"source": "Infante", "target": "Duke", "type":"A"},
        {"source": "Duke", "target": "Gordon", "type":"A"},
        {"source": "Duke", "target": "Sylvester", "type":"A"},
        {"source": "Baron", "target": "Duke", "type":"A"},
        {"source": "Baron", "target": "Sylvester", "type":"E"},
        {"source": "Evette", "target": "Sylvester", "type":"E"},
        {"source": "Cynthia", "target": "Sylvester", "type":"E"},
        {"source": "Cynthia", "target": "Jamie", "type":"E"},
        {"source": "Mauer", "target": "Jessie", "type":"E"}
    ];

    const svg = d3.select("#app").select("svg");
    const height = +svg.attr("height");
    const width = +svg.attr("width");

    const simulation = d3.forceSimulation()
        .nodes(nodes_data);

    //also going to add a centering force
    simulation
        .force("charge_force", d3.forceManyBody())
        .force("center_force", d3.forceCenter(width / 2, height / 2));

    // g - group element
    const node = svg.append("g")
        .attr("class", "nodes")
        .selectAll("circle")
        .data(nodes_data)
        .join("circle")
        // .enter().append("circle")
        .attr("r", 10)
        .attr("fill", node => node.sex === "M" ? "blue" : "pink");

    const link_force = d3.forceLink(links_data)
        .id(data => data.name); // from node_data

    simulation.force("links", link_force);

    const link = svg.append("g")
        .attr("class", "links")
        .selectAll("line")
        .data(links_data)
        // .enter().append("line")
        .join("line")
        .attr("stroke-width", 2)
        .style("stroke", link => link.type === "A" ? "green" : "red");

    const dragHandler = d3.drag()
        .on("start", d => dragStart(d, simulation))
        .on("drag", d => dragDrag(d))
        .on("end", d => dragEnd(d, simulation));

    dragHandler(node);

    simulation.on("tick", () => tickActions(node, link));
};
