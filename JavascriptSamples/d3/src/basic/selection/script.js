window.onload = function () {
    function getRandomInt(min, max) {
        return Math.floor(Math.random() * Math.floor(max) + min);
    }

    function randomData() {
        let data = [];
        let nodeCount = getRandomInt(1, 5);

        for (let i = 0; i < nodeCount; ++i) {
            data.push(getRandomInt(0, 100));
        }

        return data;
    }

    function update() {
        let p = d3.select("#app")
            .selectAll("p") // https://bost.ocks.org/mike/join/
            .data(randomData())
            .text(i => `Index: ${i}`); // update existing nodes

        p.enter().append("p").text(i => `Index: ${i}`); // create new nodes

        p.exit().remove(); // remove nodes if new data is smaller than previous
    }

    setInterval(update, 1000);
};