import './Button.css'

type ButtonProps = {
    text: string;
    isDisabled?: boolean;
    onClick: (event: React.MouseEvent<HTMLButtonElement>) => void;
};

export default function Button({ text, isDisabled, onClick }: ButtonProps) {
    return (
        <button className="button" type="button" disabled={isDisabled} onClick={onClick}>
            {text}
        </button>
    )
}