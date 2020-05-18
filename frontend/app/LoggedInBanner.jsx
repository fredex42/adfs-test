import React from 'react';

class LoggedInBanner extends React.Component {
    render() {
        const tokenValue = sessionStorage.getItem("adfs-test:token");

    }
}

export default LoggedInBanner;