import React from 'react';
import PropTypes from 'prop-types';
import {Link} from 'react-router-dom';

class RootComponent extends React.Component {
    static propTypes = {
        onLoggedIn: PropTypes.func.isRequired,
        onLoggedOut: PropTypes.func.isRequired,
        currentUsername: PropTypes.string,
        isLoggedIn: PropTypes.bool.isRequired
    };

    render() {
        //need to use href for logout not Link as it is not part of the frontend
        return(<div>
            <ul>
                <li><Link to="/about_me/profile">My user profile</Link></li>
                <li><a href="/logout">Log out</a></li>
            </ul>
        </div>);
    }
}

export default RootComponent;