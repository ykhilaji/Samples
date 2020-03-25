window.onload = function () {
    function getRandomInt(min, max) {
        return Math.floor(Math.random() * Math.floor(max) + min);
    }

    function randomData() {
        let data = [];

        for (let i = 0; i < 5; ++i) {
            data.push(getRandomInt(0, 100));
        }

        return data;
    }

    function update() {
        const data = randomData();
        const scale = d3.scaleLinear()
            .domain([0, d3.max(data)])
            .range([0, 420]);

        const div = d3.select("#app")
            .style("font", "10px sans-serif")
            .style("text-align", "right")
            .style("color", "white");

        div.selectAll("div")
            .data(randomData())
            .join("div")
            .transition()
            .duration(500)
            .style("background", "steelblue")
            .style("padding", "3px")
            .style("margin", "1px")
            .style("width", d => `${scale(d)}px`)
            .text(d => d);
    }

    update();
    setInterval(update, 3000);
};