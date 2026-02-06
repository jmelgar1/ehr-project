import { useState } from "react";
import { useAuth } from "../contexts/useAuth";
import './LoginPage.css'
import Button from "../components/Button";
import { useNavigate } from "react-router-dom";
import logo from '../assets/justmind.png'

export default function LoginPage() {
    const authContext = useAuth();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    async function loginToHome() {
        const success = await authContext.login(username, password);
        if(success) {
            navigate("/home");
        }   
    }

    return (
        <form>
            <img src={logo} alt="Logo" />
            <p className="tagline">A unique solution for clinics looking to better manage their practice through innovative and practical technologies.</p>
            <p className="error-message">{authContext.error}</p>

            <label>
                <span>Username</span>
                <input value={username} onChange={(e) => setUsername(e.target.value)}/>
            </label>

            <label>
                <span>Password</span>
                <input value={password} onChange={(e) => setPassword(e.target.value)} type={"password"}/>
            </label>

            <Button 
                text='Login' 
                isDisabled={username == '' || password == ''} 
                onClick={loginToHome}
            />

        </form>
    )
}
