import logo from '../../assets/justmind_dark.png'
import profileIcon from '../../assets/profile_icon.png'
import calendarIcon from '../../assets/calendar.png'
import patientIcon from '../../assets/patient.png'
import sessionIcon from '../../assets/session.png'
import adminIcon from '../../assets/admin.png'
import { useEffect, useRef, useState } from "react";
import { useAuth } from "../../contexts/useAuth";
import { UserRole } from "../../types/enums/UserRole";
import './Header.css';
import NavCell from "./NavCell";
import { useNavigate } from 'react-router-dom'

export default function Header() {
    const authContext = useAuth();
    const [searchInput, setSearchInput] = useState('');
    const [isDropDownOpen, setIsDropDownOpen] = useState(false);
    const dropDownRef = useRef<HTMLDivElement | null>(null);
    const navigate = useNavigate();

    function logout() {
        authContext.logout();
        navigate("/login")
    }

    useEffect(() => {
        if(!isDropDownOpen) return;

        const handleClick = (event: MouseEvent) => {
            if(dropDownRef.current && !dropDownRef.current.contains(event.target as HTMLDivElement)) {
                setIsDropDownOpen(false);
            };
        }

        document.addEventListener('click', handleClick)

        return () => {
            document.removeEventListener('click', handleClick);
        };
    }, [isDropDownOpen]);

    return (
        <header className="header">
            <img src={logo} alt="Logo" className="header-logo" />

            <nav className="header-nav">

                <NavCell to="/calendar" icon={calendarIcon} label="Calendar"/>
                <NavCell to="/patient" icon={patientIcon} label="Patient"/>
                <NavCell to="/sessions" icon={sessionIcon} label="Sessions"/>

                {authContext.user?.role === UserRole.ADMIN && (
                    <NavCell to="/admin" icon={adminIcon} label="Admin"/>
                )}
            </nav>

            <div className="header-right">
                <input
                    value={searchInput}
                    placeholder="Search anything"
                    onChange={(e) => setSearchInput(e.target.value)}
                    className="header-search"
                />

                <div className="profile-container" ref={dropDownRef}>
                    <button className="profile-button" onClick={() => setIsDropDownOpen(!isDropDownOpen)}>
                        <img src={profileIcon} alt="Profile" className="profile-icon" />
                    </button>

                    {isDropDownOpen && (
                        <div className="profile-dropdown">
                            <button className="dropdown-item" onClick={() => navigate("/settings")}>Settings</button>
                            <button className="dropdown-item" onClick={logout}>Logout</button>
                        </div>
                     )}
                </div>
            </div>
        </header>
    )
}