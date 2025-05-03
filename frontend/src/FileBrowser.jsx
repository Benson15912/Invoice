import React, { useState, useEffect } from "react";
import axios from "axios";

const FileBrowser = () => {
  const [path, setPath] = useState(['pdf-storage']); // Start with 'pdf-storage' as root
  const [columns, setColumns] = useState([]); // Each is a list of files/folders

  const fetchItems = async (currentPath) => {
    const pathStr = currentPath.join('/');  // Modify path format
    try {
      const response = await axios.get(`/api/storage/listfolders?path=${pathStr}`);
      return response.data;
    } catch (error) {
      console.error("Error fetching items", error);
      return [];
    }
  };

  useEffect(() => {
    // Load root folder (pdf-storage) on mount
    fetchItems(['pdf-storage']).then(items => {
      setColumns([items]);
    });
  }, []);

  const handleClick = async (level, item) => {
    const newPath = [...path.slice(0, level), item.name];
    setPath(newPath);

    if (item.type === 'folder') {
      const newItems = await fetchItems(newPath);
      setColumns([
        ...columns.slice(0, level + 1),
        newItems
      ]);
    } else {
      // File clicked: just update the path, not columns
      setColumns(columns.slice(0, level + 1));
    }
  };

  const getSelectedItem = () => {
    let selected = null;
    for (let i = 0; i < path.length; i++) {
      const col = columns[i];
      selected = col?.find(item => item.name === path[i]) || null;
    }
    return selected?.type === 'file' ? selected : null;
  };

  return (
    <div className="flex h-screen">
      {columns.map((items, level) => (
        <div key={level} className="w-1/4 border-r overflow-y-auto bg-white">
          {items.map((item) => (
            <div
              key={item.name}
              className={`p-2 cursor-pointer hover:bg-gray-100 ${
                path[level] === item.name ? "bg-cyan-600 text-white" : ""
              }`}
              onClick={() => handleClick(level, item)}
            >
              <span className="mr-2">{item.type === "folder" ? "📁" : "📄"}</span>
              {item.name.replace(".pdf", "")}
            </div>
          ))}
        </div>
      ))}
      <div className="w-1/4 p-4">
        <h2 className="text-xl font-bold mb-2">Preview</h2>
        {getSelectedItem() ? (
          <div className="border p-2 rounded-lg bg-gray-50">
            <embed
              src={`http://localhost:8080/api/storage/view?filepath=${path.join('/')}`}
              type="application/pdf"
              width="50%"
              height="1000px"
            />
          </div>
        ) : (
          <p>No file selected</p>
        )}
      </div>
    </div>
  );
};

export default FileBrowser;
