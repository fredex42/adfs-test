import React from 'react';

/**
 * makes an authenticated REST request to /api/profile based on the JWT in the session
 */
class TestRequestComponent extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            loading: false,
            returnedData: "",
            lastError: null
        }
    }


}

export default TestRequestComponent;