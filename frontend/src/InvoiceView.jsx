import { useState, useEffect } from "react";
import "./invoice.css";

const List = ({ list, setDisplayPath }) => {
    const [isExpanded, setIsExpanded] = useState({});

    const handleToggle = (nodeName) => {
        setIsExpanded((prev) => ({
            ...prev,
            [nodeName]: !prev[nodeName],
        }));
    };

    const handleFileClick = (path) => {
        setDisplayPath(path); // Now this will work
    };

    return (
        <div className="container">
            {list.map((node) => (
                <div className="container" key={node.path}>
                    {node.isFolder && (
                        <span
                            onClick={() => handleToggle(node.name)}
                            style={{ cursor: "pointer" }}
                        >
                            {isExpanded[node.name] ? "− " : "+ "}
                        </span>
                    )}
                    {!node.isFolder && (
                        <span
                            style={{ cursor: "pointer"}}
                            onClick={() => handleFileClick(node.path)}
                            >
                            {node.name}
                        </span>
                    )}
                    <span> {node.isFolder && <img className = "icon" src ='resources/logos/folder.png' /> } {node.isFolder && node.name}</span>
                    {isExpanded[node.name] && node.children && (
                        <List list={node.children} setDisplayPath={setDisplayPath} />
                    )}
                </div>
            ))}
        </div>
    );
};

function InvoiceView() {
    const [data, setData] = useState([]);
    const [displayPath, setDisplayPath] = useState("");
    useEffect(() => {
        fetch("api/storage/listfoldertree")
            .then((response) => {
                if (!response.ok) {
                    throw new Error("Failed to fetch data");
                }
                return response.json(); // convert response to JSON
            })
            .then((jsonData) => {
                setData(jsonData); // store JSON in data state
            })
            .catch((error) => {
                console.error("Error fetching JSON:", error);
            });
    }, []);

    return (
        <div className="main-view">
            <div className="filebox">
                <List list={data} setDisplayPath={setDisplayPath} />
            </div>
            <div className="previewbox">
                {displayPath && (
                    <embed
                        className="pdf-view"
                        src={`http://localhost:8080/api/storage/view?filepath=${encodeURIComponent(displayPath)}`}
                        type="application/pdf"
                        />
                )}
            </div>
        </div>
    );
}

export default InvoiceView;
