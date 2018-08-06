import React from 'react'
import ReactDOM from 'react-dom'
import {HashRouter, Link, Route, Switch, Redirect} from 'react-router-dom'

const PageTemplate = ({children}) => {
    return (
        <div>
            <div>
                <nav>
                    <Link to="/">Home</Link>
                    <Link to="about">About</Link>
                </nav>
            </div>
            {children}
        </div>
    )
};

const Home = () => {
    return (
        <PageTemplate>
            <p>Home page</p>
        </PageTemplate>
    )
};

const About = () => {
    return (
        <PageTemplate>
            <p>About page</p>
        </PageTemplate>
    )
};

const Error404 = () => {
    return (
        <PageTemplate>
            <p>Error 404</p>
        </PageTemplate>
    )
};


ReactDOM.render(
    <div>
        <HashRouter>
            <Switch>
                <Route exact path="/" component={Home}/>
                <Route path="/about" component={About}/>
                <Redirect from="/unknown" to="/about"/>
                <Route component={Error404}/>
            </Switch>
        </HashRouter>
    </div>,
    document.getElementById("root")
);

