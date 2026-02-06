import { useNavigate } from "react-router-dom";
import Button from "../components/Button";
import { useAuth } from "../contexts/useAuth";
import './HomePage.css'

export default function HomePage() {
    const authContext = useAuth();
    const navigate = useNavigate();

    function logout() {
        authContext.logout();
        navigate("/login")
    }

    return (
        <form>
            <label>HOME PAGE</label>

            <Button 
                text='Logout' 
                onClick={logout}/>
        </form>
    )
}