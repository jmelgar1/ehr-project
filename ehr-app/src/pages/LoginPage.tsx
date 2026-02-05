import { useState } from "react";
import { useAuth } from "../contexts/AuthContext";
import './LoginPage.css'
import Button from "../components/Button";
import { useNavigate } from "react-router-dom";

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
            <label>
                Username
                <input value={username} onChange={(e) => setUsername(e.target.value)}/>
            </label>

            <label>
                Password
                <input value={password} onChange={(e) => setPassword(e.target.value)} type={"password"}/>
            </label>

            <Button text='Login' onClick={loginToHome}/>
        </form>
    )
}
