import React from 'react';
import {render} from 'react-dom';
import {BrowserRouter, Link, Route, Switch, Redirect, withRouter} from 'react-router-dom';
import RootComponent from './RootComponent.jsx';
import UserProfileComponent from "./UserProfileComponent.jsx";
import UrlNotFoundComponent from "./UrlNotFoundComponent.jsx";
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

        this.onLoggedIn = this.onLoggedIn.bind(this);
        this.onLoggedOut = this.onLoggedOut.bind(this);

        this.returnToRoot = this.returnToRoot.bind(this);
    }

    returnToRoot(){
        this.props.history.push("/");
    }

    checkLogin(){
        return new Promise((resolve,reject)=>
            reject("not implemented")
        );
    }

    componentDidMount(){
        // this.checkLogin().then(()=>{
        //     if(!this.state.loading && !this.state.isLoggedIn) {
        //         this.setState({redirectingTo: window.location.href });
        //     }
        // })
    }

    onLoggedIn(userid, isAdmin){
        console.log("Logged in as " + userid);
        console.log("Is an admin? " + isAdmin);

        this.setState({currentUsername: userid, isAdmin: isAdmin, isLoggedIn: true}, ()=>{
            if(this.state.redirectingTo){
                window.location.href = this.state.redirectingTo;
            } else {
                if (!isAdmin) window.location.href = "/about_me/profile";
            }
        })
    }

    onLoggedOut(){
        this.setState({currentUsername: "", isLoggedIn: false})
    }


    render(){
        // if(!this.state.loading && !this.state.isLoggedIn) {
        //     return <div>
        //         <LoginComponent onLoggedIn={this.onLoggedIn} onLoggedOut={this.onLoggedOut} currentlyLoggedIn={this.state.isLoggedIn}/>
        //     </div>
        // }
        return <div>
            <h1 onClick={this.returnToRoot} className="clickable">adfs-test</h1>
            <Switch>
                <Route path="/about_me/profile" component={UserProfileComponent}/>
                <Route exact path="/" component={()=><RootComponent
                    onLoggedOut={this.onLoggedOut}
                    onLoggedIn={this.onLoggedIn}
                    currentUsername={this.state.currentUsername}
                    isLoggedIn={this.state.isLoggedIn}
                    isAdmin={this.state.isAdmin}
                />}/>
                <Route path="/" component={UrlNotFoundComponent}/>
            </Switch>
        </div>
    }
}

const AppWithRouter = withRouter(App);

render(<BrowserRouter root="/"><AppWithRouter/></BrowserRouter>, document.getElementById('app'));