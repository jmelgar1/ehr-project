import { NavLink } from "react-router-dom";
import logo from '../assets/justmind.png'
import profileIcon from '../assets/profile_icon.png'
import { useState } from "react";
import { useAuth } from "../contexts/useAuth";
import { UserRole } from "../types/enums/UserRole";
import './Header.css';

export default function Header() {
    const authContext = useAuth();
    const [searchInput, setSearchInput] = useState('');

    return (
        <header className="header">
            <img src={logo} alt="Logo" className="header-logo" />

            <nav className="header-nav">
                <NavLink to="/calendar" className={({ isActive }) => isActive ? "tab active" : "tab"}>
                    Calendar
                </NavLink>

                <NavLink to="/patient" className={({ isActive }) => isActive ? "tab active" : "tab"}>
                    Patient
                </NavLink>

                <NavLink to="/sessions" className={({ isActive }) => isActive ? "tab active" : "tab"}>
                    Sessions
                </NavLink>

                {authContext.user?.role === UserRole.ADMIN && (
                    <NavLink to="/admin" className={({ isActive }) => isActive ? "tab active" : "tab"}>
                        Admin
                    </NavLink>
                )}
            </nav>

            <div className="header-right">
                <input
                    value={searchInput}
                    placeholder="Search anything"
                    onChange={(e) => setSearchInput(e.target.value)}
                    className="header-search"
                />
                <button className="profile-button">
                    <img src={profileIcon} alt="Profile" className="profile-icon" />
                </button>
            </div>
        </header>
    )
    /**
     * Header navigation bar plan
     * 
     * Far left side we want the JustMind logo.
     * Far right side we want the profile_icon.png which will be a button that opens up profile settings.
     * TO the left of the profile icon there will be a search bar where its basically a full on wildcard search.
     * (not sure how feasible this would be but we can just implement a search bar for now with little to no functionality)
     * 
     * IN between those two things we will have our navigation buttons.
     * For now we can just do "Calendar", "Patient", "Admin" (only visible to admins), and "Sessions"
     * Right now we will just have place holders that will navigate to nothing so we can start with just one
     */
}