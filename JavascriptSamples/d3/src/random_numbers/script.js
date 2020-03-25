window.onload = function () {
    function getRandomInt(min, max) {
        return Math.floor(Math.random() * Math.floor(max) + min);
    }

    function randomData() {
        let data = [];
        let nodeCount = getRandomInt(1, 5);

        for (let i = 0; i < nodeCount; ++i) {
            data.push(getRandomInt(0, 10));
        }

        return data;
    }

    function update(svg) {
        const transition = svg.transition().duration(750);

        // https://observablehq.com/@d3/selection-join
        svg.selectAll("text")
            .data(randomData(), d => d)
            .join(
                enter => enter.append("text")
                    .attr("fill", "green")
                    .attr("x", (d, i) => i * 16)
                    .attr("y", -30)
                    .text(d => ` ${d} `)
                    .call(enter => enter.transition(transition)
                        .attr("y", 0)),
                update => update
                    .attr("fill", "black")
                    .attr("y", 0)
                    .call(update => update.transition(transition)
                        .attr("x", (d, i) => i * 16)),
                exit => exit
                    .attr("fill", "brown")
                    .call(exit => exit.transition(transition)
                        .attr("y", 30)
                        .remove())
            );
    }

    const svg = d3.select("#app")
        .append("svg")
        .attr("width", 100)
        .attr("height", 33)
        .attr("viewBox", `0 -20 100 33`);

    setInterval(() => update(svg), 1000);
};