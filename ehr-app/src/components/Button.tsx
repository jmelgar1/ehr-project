import './Button.css'

type ButtonProps = {
    text: string;
    onClick: (event: React.MouseEvent<HTMLButtonElement>) => void;
};

export default function Button({ text, onClick }: ButtonProps) {
    return (
        <button type="button" onClick={onClick}>
            {text}
        </button>
    )
}