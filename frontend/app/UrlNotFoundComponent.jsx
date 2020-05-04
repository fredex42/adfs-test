import React from 'react';
import {Link} from 'react-router-dom';

class UrlNotFoundComponent extends React.Component {
    render(){
        return <div>
            <h2>not found</h2>
            <p>Hmm, the url you put in <pre>{this.props.location.pathname}</pre> does not seem to exist in the frontend.
                Maybe you meant one of these?</p>
            <ul>
                <li><Link to="/about_me/profile">/about_me/profile</Link></li>
            </ul>
        </div>
    }
}

export default UrlNotFoundComponent;