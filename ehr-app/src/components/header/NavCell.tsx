import { NavLink } from "react-router-dom";
import './NavCell.css';

type NavCellProps = {
    to: string;
    icon: string;
    label: string;
};

export default function NavCell({ to, icon, label }: NavCellProps) {
    return (
        <NavLink to={to} className={({ isActive }) => isActive ? "nav-cell active" : "nav-cell"}>
            <img src={icon} alt="" className="nav-cell-icon" />
            {label}
        </NavLink>
    )
}