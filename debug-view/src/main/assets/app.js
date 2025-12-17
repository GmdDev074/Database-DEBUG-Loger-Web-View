/**
 * Android Debug Database Logic
 * 
 * APPROACH:
 * - OOP: `DatabaseApp` class manages state, UI references, and event handling.
 * - Helpers: Specialized classes for different DB Types (KeyValue vs Tables).
 */

// ==========================================
// Functional Utilities
// ==========================================

const createRowHtml = (cells) => {
    const cellHtml = cells.map((cell, index) => {
        const style = index === 0 ? 'font-family: monospace; font-weight: 500;' : 'color: #475569;';
        return `<td style="${style}">${cell}</td>`;
    }).join('');
    return `<tr>${cellHtml}</tr>`;
};

const createHeaderHtml = (headers) => {
    return headers.map(h => `<th>${h}</th>`).join('');
};

// ==========================================
// Database Helpers (Strategies)
// ==========================================

class DatabaseHelper {
    constructor(name) {
        this.name = name;
    }
    getHeaders() { return []; }
    getData() { return []; }
    getType() { return 'generic'; }
}

class KeyValueHelper extends DatabaseHelper {
    constructor(name, data) {
        super(name);
        this.data = data || [];
    }

    getHeaders() { return ['Key', 'Value']; }

    getData() {
        return this.data.map(item => [item.key, item.value]);
    }

    getType() { return APP_CONSTANTS.DB_TYPES.SHARED_PREFS; }
}

class TableHelper extends DatabaseHelper {
    constructor(name, headers, data) {
        super(name);
        this.headers = headers || [];
        this.data = data || []; // Array of Arrays or Objects
    }

    getHeaders() { return this.headers; }

    getData() {
        // Assume data is array of arrays matching headers order
        // In real SQLite, rows are often objects, so we map them to headers
        if (this.data.length > 0 && !Array.isArray(this.data[0])) {
            return this.data.map(row => this.headers.map(h => row[h] || 'NULL'));
        }
        return this.data;
    }

    getType() { return APP_CONSTANTS.DB_TYPES.ROOM_SQLITE; }
}

// ==========================================
// Application Logic
// ==========================================

class DatabaseApp {
    constructor() {
        // State
        this.databases = []; // Fetched dynamically
        this.activeDbHelper = null;
        this.currentDbName = null;
        this.currentTable = null;

        // UI References
        this.ui = {
            dbList: document.getElementById('db-list'),
            tableList: document.getElementById('table-list'),
            dataRows: document.getElementById('data-rows'),
            tableHeaderRow: document.querySelector('.data-table thead tr'),
            searchInput: document.getElementById('search-data'),
            reachMeMenu: document.getElementById('reach-me-menu'),
            otherProjectsMenu: document.getElementById('other-projects-menu')
        };

        this.init();
    }

    async init() {
        this.renderNavbarMenus();
        await this.loadDatabaseList();
        this.attachEventListeners();
    }

    async loadDatabaseList() {
        try {
            const res = await fetch('/getDbList');
            if (!res.ok) throw new Error('Failed to fetch DB list');
            const json = await res.json();
            // Expecting format: { rows: [ {name: 'db1', type: 'SHARED_PREFS'}, ... ] }
            this.databases = json.rows || [];

            this.renderDatabases();

            if (this.databases.length > 0) {
                this.loadDatabase(this.databases[0].name);
            } else {
                this.ui.dbList.innerHTML = '<li class="list-item">No Databases Found</li>';
            }
        } catch (e) {
            console.error(e);
            this.ui.dbList.innerHTML = '<li class="list-item" style="color:red">Connection Failed</li>';
        }
    }

    renderNavbarMenus() {
        // Render Reach Me (Socials)
        this.ui.reachMeMenu.innerHTML = APP_CONSTANTS.SOCIAL_LINKS.map(link => {
            // Fix: LinkedIn often blocked or fails on generic CDNs, use inline SVG for robustness
            if (link.icon === 'linkedin') {
                return `
                    <a href="${link.url}" target="_blank" class="dropdown-item social-item">
                        <svg class="social-icon" role="img" viewBox="0 0 24 24" fill="#${link.color}" xmlns="http://www.w3.org/2000/svg"><title>${link.name}</title><path d="M20.447 20.452h-3.554v-5.569c0-1.328-.027-3.037-1.852-3.037-1.853 0-2.136 1.445-2.136 2.939v5.667H9.351V9h3.414v1.561h.046c.477-.9 1.637-1.85 3.37-1.85 3.601 0 4.267 2.37 4.267 5.455v6.286zM5.337 7.433c-1.144 0-2.063-.926-2.063-2.065 0-1.138.92-2.063 2.063-2.063 1.14 0 2.064.925 2.064 2.063 0 1.139-.925 2.065-2.064 2.065zm1.782 13.019H3.555V9h3.564v11.452z"/></svg>
                        ${link.name}
                    </a>
                `;
            }

            // Using SimpleIcons CDN for clean SVGs
            // For IMO, we might fallback or use a generic if specific not found, but trying to use relevant slug or the color provided.
            const iconUrl = link.name === 'IMO'
                ? `https://cdn.simpleicons.org/googlemessages/${link.color}` // Fallback to a message lookalike
                : `https://cdn.simpleicons.org/${link.icon}/${link.color}`;

            return `
                <a href="${link.url}" target="_blank" class="dropdown-item social-item">
                    <img src="${iconUrl}" alt="${link.name}" class="social-icon">
                    ${link.name}
                </a>
            `;
        }).join('');

        // Render Other Projects
        this.ui.otherProjectsMenu.innerHTML = APP_CONSTANTS.OTHER_PROJECTS.map(proj =>
            `<a href="${proj.url}" target="_blank" class="dropdown-item">${proj.name}</a>`
        ).join('');
    }

    renderDatabases() {
        this.ui.dbList.innerHTML = this.databases.map(db => {
            const isActive = db.name === this.currentDbName ? 'active' : '';
            // We use data-type to know which helper to load
            return `<li class="list-item ${isActive}" data-name="${db.name}" data-type="${db.type}">${db.name}</li>`;
        }).join('');
    }

    async loadDatabase(dbName) {
        this.currentDbName = dbName;
        // Find stored type info
        const dbInfo = this.databases.find(d => d.name === dbName) || {};

        // Fetch tables for the selected DB
        const tables = await this._fetchTables(dbName);
        this.renderTables(tables);

        // Select first table by default if available
        if (tables.length > 0) {
            this.loadTable(dbName, tables[0], dbInfo.type);
        } else {
            this.ui.dataRows.innerHTML = '';
            this.ui.tableHeaderRow.innerHTML = '';
        }
    }

    async loadTable(dbName, tableName, type) {
        this.currentTable = tableName;

        // Update Table UI Selection
        const listItems = this.ui.tableList.querySelectorAll('.list-item');
        listItems.forEach(item => {
            if (item.dataset.value === tableName) item.classList.add('active');
            else item.classList.remove('active');
        });

        // Instantiate Helper
        if (type === APP_CONSTANTS.DB_TYPES.SHARED_PREFS) {
            const data = await this._fetchData(dbName, tableName);
            this.activeDbHelper = new KeyValueHelper(dbName, data);
        } else if (type === APP_CONSTANTS.DB_TYPES.ROOM_SQLITE) {
            const response = await this._fetchData(dbName, tableName);
            // Response expected: { columns: [], rows: [] }
            this.activeDbHelper = new TableHelper(dbName, response.columns, response.rows);
        } else if (type === APP_CONSTANTS.DB_TYPES.PAPER_DB) {
            const data = await this._fetchData(dbName, tableName);
            this.activeDbHelper = new KeyValueHelper(dbName, data);
        } else {
            // Default/Fallback
            const data = await this._fetchData(dbName, tableName);
            this.activeDbHelper = new KeyValueHelper(dbName, data || []);
        }

        this.renderData();
    }

    renderTables(tables) {
        this.ui.tableList.innerHTML = tables.map((table, idx) => {
            const isActive = idx === 0 ? 'active' : ''; // Select first by default
            return `<li class="list-item ${isActive}" data-value="${table}">${table}</li>`;
        }).join('');
    }

    renderData(searchTerm = '') {
        if (!this.activeDbHelper) return;

        // 1. Set Headers
        this.ui.tableHeaderRow.innerHTML = createHeaderHtml(this.activeDbHelper.getHeaders());

        // 2. Filter & Get Data
        let data = this.activeDbHelper.getData();

        if (searchTerm) {
            const lowerTerm = searchTerm.toLowerCase();
            data = data.filter(row =>
                row.some(cell => String(cell).toLowerCase().includes(lowerTerm))
            );
        }

        // 3. Render
        if (data.length === 0) {
            this.ui.dataRows.innerHTML = `<tr><td colspan="${this.activeDbHelper.getHeaders().length}" style="text-align:center; color: #94a3b8; padding: 2rem;">No entries found</td></tr>`;
            return;
        }

        this.ui.dataRows.innerHTML = data.map(row => createRowHtml(row)).join('');
    }

    // --- API Calls (Production) ---

    async _fetchTables(dbName) {
        try {
            const res = await fetch(`/getTableList?dbName=${encodeURIComponent(dbName)}`);
            if (!res.ok) throw new Error('API Failed');
            const json = await res.json();
            return json.rows;
        } catch (e) {
            console.error("API Error:", e);
            return [];
        }
    }

    async _fetchData(dbName, tableName) {
        try {
            const res = await fetch(`/getAllData?dbName=${encodeURIComponent(dbName)}&tableName=${encodeURIComponent(tableName)}`);
            if (!res.ok) throw new Error('API Failed');
            const json = await res.json();
            // For SQL: { columns: [...], rows: [...] }
            // For KV:  [ {key, value}, ... ]
            return json;
        } catch (e) {
            console.error("API Error:", e);
            return [];
        }
    }

    attachEventListeners() {
        // DB Selection
        this.ui.dbList.addEventListener('click', (e) => {
            const target = e.target.closest('.list-item');
            if (target) {
                this.loadDatabase(target.dataset.name);
            }
        });

        // Table Selection
        this.ui.tableList.addEventListener('click', (e) => {
            const target = e.target.closest('.list-item');
            if (target) {
                const dbName = this.currentDbName;
                const tableName = target.dataset.value;
                // Find DB Info locally to get Type
                const dbInfo = this.databases.find(d => d.name === dbName);
                if (dbInfo) {
                    this.loadTable(dbName, tableName, dbInfo.type);
                }
            }
        });

        // Search
        this.ui.searchInput.addEventListener('input', (e) => {
            this.renderData(e.target.value);
        });
    }
}

// Initialize Application
document.addEventListener('DOMContentLoaded', () => {
    window.app = new DatabaseApp();
});
