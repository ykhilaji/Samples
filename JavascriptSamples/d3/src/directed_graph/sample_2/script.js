// https://observablehq.com/@mbostock/mobile-patent-suits

window.onload = function () {
    function linkArc(d) {
        const r = Math.hypot(d.target.x - d.source.x, d.target.y - d.source.y);
        return `
    M${d.source.x},${d.source.y}
    A${r},${r} 0 0,1 ${d.target.x},${d.target.y}
  `;
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

    const nodes =  [
        {"name": "Lillian"},
        {"name": "Gordon"},
        {"name": "Sylvester"},
        {"name": "Mary"},
        {"name": "Helen"},
        {"name": "Jamie"},
        {"name": "Jessie"},
        {"name": "Ashton"},
        {"name": "Duncan"},
        {"name": "Evette"},
        {"name": "Mauer"},
        {"name": "Fray"},
        {"name": "Duke"},
        {"name": "Baron"},
        {"name": "Infante"},
        {"name": "Percy"},
        {"name": "Cynthia"}
    ];

    const links = [
        {"source": "Sylvester", "target": "Gordon" },
        {"source": "Sylvester", "target": "Lillian" },
        {"source": "Sylvester", "target": "Mary"},
        {"source": "Sylvester", "target": "Jamie"},
        {"source": "Sylvester", "target": "Jessie"},
        {"source": "Sylvester", "target": "Helen"},
        {"source": "Helen", "target": "Gordon"},
        {"source": "Mary", "target": "Lillian"},
        {"source": "Ashton", "target": "Mary"},
        {"source": "Duncan", "target": "Jamie"},
        {"source": "Gordon", "target": "Jessie"},
        {"source": "Sylvester", "target": "Fray"},
        {"source": "Fray", "target": "Mauer"},
        {"source": "Fray", "target": "Cynthia"},
        {"source": "Fray", "target": "Percy"},
        {"source": "Percy", "target": "Cynthia"},
        {"source": "Infante", "target": "Duke"},
        {"source": "Duke", "target": "Gordon"},
        {"source": "Duke", "target": "Sylvester"},
        {"source": "Baron", "target": "Duke"},
        {"source": "Baron", "target": "Sylvester"},
        {"source": "Evette", "target": "Sylvester"},
        {"source": "Cynthia", "target": "Sylvester"},
        {"source": "Cynthia", "target": "Jamie"},
        {"source": "Mauer", "target": "Jessie"}
    ];

    const svg = d3.select("#app").select("svg");
    const height = +svg.attr("height");
    const width = +svg.attr("width");

    const simulation = d3.forceSimulation(nodes)
        .force("link", d3.forceLink(links).id(d => d.name))
        .force("charge", d3.forceManyBody().strength(-400))
        .force("center_force", d3.forceCenter(width / 2, height / 2));
        // .force("x", d3.forceX())
        // .force("y", d3.forceY());

    const dragHandler = d3.drag()
        .on("start", d => dragStart(d, simulation))
        .on("drag", d => dragDrag(d))
        .on("end", d => dragEnd(d, simulation));

    svg.append("defs").selectAll("marker")
        .data(["marker1"])
        .join("marker")
        .attr("id", d => `arrow-${d}`)
        .attr("viewBox", "0 -5 10 10")
        .attr("refX", 15)
        .attr("refY", -0.5)
        .attr("markerWidth", 6)
        .attr("markerHeight", 6)
        .attr("orient", "auto")
        .append("path")
        .attr("fill", "orange")
        .attr("d", "M0,-5L10,0L0,5");

    const link = svg.append("g")
        .attr("fill", "none")
        .attr("stroke-width", 1.5)
        .selectAll("path")
        .data(links)
        .join("path")
        .attr("stroke", "red")
        // https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/marker-end
        .attr("marker-end", d => `url(${new URL(`#arrow-marker1`, location)})`);

    const node = svg.append("g")
        .attr("fill", "currentColor")
        .attr("stroke-linecap", "round")
        .attr("stroke-linejoin", "round")
        .selectAll("g")
        .data(nodes)
        .join("g")
        .call(d => dragHandler(d));

    node.append("circle")
        .attr("stroke", "white")
        .attr("stroke-width", 1.5)
        .attr("r", 4);

    node.append("text")
        .attr("x", 8)
        .attr("y", "0.31em")
        .text(d => d.name)
        .clone(true).lower()
        .attr("fill", "none")
        .attr("stroke", "white")
        .attr("stroke-width", 3);

    simulation.on("tick", () => {
        link.attr("d", linkArc);
        node.attr("transform", d => `translate(${d.x},${d.y})`);
    });
};
