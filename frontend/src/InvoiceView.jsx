import { useState, useEffect } from "react";
import "./invoice.css";

const List = ({ list, setDisplayPath, refreshData }) => {
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

    const handleDelete = async (path) => {
        if (!window.confirm(`Are you sure you want to delete ${path}?`)) return;
        try {
            const response = await fetch(`/api/storage/delete?filePath=${encodeURIComponent(path)}`, {
                method: "DELETE",
            });
            if (response.ok) {
                refreshData();
            } else {
                alert("Failed to delete the file/folder.");
            }
        } catch (error) {
            console.error("Delete error:", error);
        }
    };

    const handleAddFolder = async (parentPath) => {
        const folderName = prompt("Enter new folder name:");
        if (!folderName) return;

        try {
            const params = new URLSearchParams({
                targetDir: parentPath,
                folderName: folderName,
            });

            const response = await fetch(`/api/storage/addfolder?${params.toString()}`, {
                method: "POST",
            });

            if (response.ok) {
                refreshData();
            } else {
                alert("Failed to create folder.");
            }
        } catch (error) {
            console.error("Add folder error:", error);
        }
    };
    return (
        <div className="container">
            {list.map((node) => (
                <div className="container" key={node.path}>
                    {node.isFolder ? (
                        <>
                    <span onClick={() => handleToggle(node.name)} style={{ cursor: "pointer" }}>
                        {isExpanded[node.name] ? "− " : "+ "}
                    </span>
                            <img className="icon" src="resources/logos/folder.png" alt="folder" />
                            <span>{node.name}&nbsp;&nbsp;</span>
                            <span className="button-icon"
                                    onClick={() => handleAddFolder(node.path)}>
                                    <img className="icon" src="resources/logos/addfolder.png" alt="add" />
                            </span>
                            <span className="button-icon delete-button"
                                    onClick={() => handleDelete(node.path)}> <img className="icon" src="resources/logos/delete.png" alt="del" />
                            </span>
                        </>
                    ) : (
                        <>
                    <span
                        style={{ cursor: "pointer", marginRight: "8px" }}
                        onClick={() => handleFileClick(node.path)}
                    >
                        {node.name}
                    </span>
                            <span className="button-icon delete-button"
                                  onClick={() => handleDelete(node.path)}> <img className="icon" src="resources/logos/delete.png" alt="del" />
                            </span>
                        </>
                    )}
                    {isExpanded[node.name] && node.children && (
                        <List list={node.children} setDisplayPath={setDisplayPath} refreshData={refreshData} />
                    )}
                </div>
            ))}
        </div>

    );
};

function InvoiceView() {
    const [data, setData] = useState([]);
    const [displayPath, setDisplayPath] = useState("");
    const refreshData = () => {
        fetch("api/storage/listfoldertree")
            .then((response) => {
                if (!response.ok) throw new Error("Failed to fetch data");
                return response.json();
            })
            .then(setData)
            .catch((error) => console.error("Error fetching JSON:", error));
    };
    useEffect(() => {
        refreshData();
    }, []);

    return (
        <div className="main-view">
            <div className="filebox">
                <List list={data} setDisplayPath={setDisplayPath} refreshData={refreshData} />
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
