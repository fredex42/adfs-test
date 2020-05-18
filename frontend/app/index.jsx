import React from 'react';
import {render} from 'react-dom';
import {BrowserRouter, Link, Route, Switch, Redirect, withRouter} from 'react-router-dom';
import RootComponent from './RootComponent.jsx';
import Raven from 'raven-js';
import { library } from '@fortawesome/fontawesome-svg-core'

import { faFolder, faFolderOpen, faTimes, faSearch, faCog } from '@fortawesome/free-solid-svg-icons'

library.add(faFolderOpen, faFolder, faTimes, faSearch, faCog);

class App extends React.Component {
    constructor(props){
        super(props);

        this.state = {
            isLoggedIn: false,
            currentUsername: "",
            isAdmin: false,
            loading: true,
            redirectingTo: null
        };
    }


    async checkLogin() {
        return true;
    }

    componentDidMount(){
        this.checkLogin().then(()=>{
            if(!this.state.loading && !this.state.isLoggedIn) {
                this.setState({redirectingTo: window.location.href });
            }
        })
    }

    returnToRoot() {
        this.props.history.push("/");
    }

    render(){
        return <div>
            <Switch>
                <Route exact path="/" component={RootComponent}/>
            </Switch>
        </div>
    }
}

const AppWithRouter = withRouter(App);

render(<BrowserRouter root="/"><AppWithRouter/></BrowserRouter>, document.getElementById('app'));