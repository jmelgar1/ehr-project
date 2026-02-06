import { Outlet } from "react-router-dom";
import Header from "../components/Header";
import './AuthenticatedLayout.css';

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