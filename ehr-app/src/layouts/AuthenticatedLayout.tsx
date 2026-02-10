import { Outlet } from "react-router-dom";
import './AuthenticatedLayout.css';
import Header from "../components/header/Header";

export default function AuthenticatedLayout() {
    return (
        <>
            <Header />
            <main className="main-content">
                <Outlet />
            </main>
        </>
    )
}