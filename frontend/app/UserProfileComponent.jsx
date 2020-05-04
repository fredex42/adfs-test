import React from 'react';

class UserProfileComponent extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            loading: false,
            lastError: null,
            profileMapData: {}
        }
    }

    setStatePromise(newState) {
        return new Promise((resolve, reject)=>this.setState(newState, ()=>resolve()));
    }

    async componentDidMount() {
        await this.setStatePromise({loading: true});
        const response = await fetch("/api/profile")
        if(response.status===200){
            const bodyContent = await response.json();
            return this.setStatePromise({loading: false, profileMapData: bodyContent});
        } else {
            console.log("Got response ", response.status);
            const errorTextContent = await response.text();
            try {
                const errorJsonContent = JSON.parse(errorTextContent);
                return this.setStatePromise({loading: false, lastError: errorJsonContent});
            } catch(err) {
                return this.setStatePromise({loading: false, lastError: errorTextContent});
            }
        }
    }

    render(){
        return <div>
            <h2>My user profile</h2>
            <p style={{display: this.state.loading ? "inherit":"none"}}>loading...</p>
            <table>
                <tbody>
                {Object.keys(this.state.profileMapData).sort().map((key,idx)=><tr key={idx}><td>{key}</td><td>{this.state.profileMapData[key]}</td></tr>)}
                </tbody>
            </table>
        </div>
    }
}

export default UserProfileComponent;