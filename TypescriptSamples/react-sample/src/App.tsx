import React, {Suspense} from 'react';
import TickComponent from "./components/TickComponent";
import ClickCounter from "./components/ClickCounter";
import Form from "./components/Form";
import ValueGenerator from "./components/ValueGenerator";
import ErrorHandler, {ThrowableComponent} from "./components/ErrorHandler";

function App() {
    const SlowComponent = React.lazy(() => {
        return import('./components/SlowComponent')
    });

    return (
    <div>
      <TickComponent/>
      <ValueGenerator/>
      <ClickCounter/>
      <Form/>

        <Suspense fallback={<div>Downloading...</div>}>
            <SlowComponent />
        </Suspense>

        <ErrorHandler>
            <ThrowableComponent/>
        </ErrorHandler>
    </div>
    );
}

export default App;
