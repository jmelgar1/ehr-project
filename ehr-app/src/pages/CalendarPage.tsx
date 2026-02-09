import { useNavigate } from "react-router-dom";
import Button from "../components/Button";
import { useAuth } from "../contexts/useAuth";
import './CalendarPage.css'

export default function CalendarPage() {
    const authContext = useAuth();

    return (
        <form>
            <label>Calendar Page</label>
        </form>
    )
}