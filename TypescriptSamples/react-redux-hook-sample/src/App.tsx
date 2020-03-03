import React from 'react';
import NewItem from "./components/NewItem";
import List from "./components/List";
import Selected from "./components/Selected";


function App() {
  return (
      <div className="App">
        <NewItem/>
        <List/>
        <Selected/>
      </div>
  );
}

export default App;