window.onload = function () {
    let title = React.createElement('h1', {}, "List of items");
    let list = React.createElement('ul', {}, ["item1", "item2", "item3", "item4", "item5"].map((el, i) => React.createElement('li', {key: i}, el)));


    let row = React.createElement('div', {'className': 'row'}, title, list);
    let container = React.createElement('div', {'className': 'container'}, row);

    ReactDOM.render(container, document.getElementById("app"));
};