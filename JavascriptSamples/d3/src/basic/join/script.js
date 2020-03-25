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
        d3.select("#app")
            .selectAll("p")
            .data(randomData())
            .join(
                enter => enter.append("p").text(i => `Index: ${i}`),
                update => update.text(i => `Index: ${i}`),
                exit => exit.remove()
            )
    }

    setInterval(update, 1000);
};