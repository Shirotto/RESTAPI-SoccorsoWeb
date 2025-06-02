/**
 * pagination.js
 * Gestione della paginazione
 */

// Variabili per la paginazione
let currentPage = 1;
let pageSize = 25;
let totalPages = 1;
let totalItems = 0;

function updatePaginationInfo(currentItems, totalItemsCount) {
    const start = totalItemsCount > 0 ? ((currentPage - 1) * pageSize) + 1 : 0;
    const end = Math.min(start + currentItems - 1, totalItemsCount);
    $('#infoPaginazione').text(`Showing ${start}-${end} of ${totalItemsCount} results`);
}

function updatePaginationControls() {
    $('#numeroPagina').text(`Pagina ${currentPage} di ${totalPages}`);
    
    $('#btnPrimaPagina, #btnPaginaPrecedente').prop('disabled', currentPage <= 1);
    $('#btnPaginaSuccessiva, #btnUltimaPagina').prop('disabled', currentPage >= totalPages);
}

// Funzioni getter e setter
function getCurrentPage() {
    return currentPage;
}

function getPageSize() {
    return pageSize;
}

function getTotalPages() {
    return totalPages;
}

function getTotalItems() {
    return totalItems;
}

function setCurrentPage(page) {
    currentPage = page;
}

function setPageSize(size) {
    pageSize = size;
}

function setTotalPages(pages) {
    totalPages = pages;
}

function setTotalItems(items) {
    totalItems = items;
}

// Esportiamo le funzioni come oggetto
const Pagination = {
    updatePaginationInfo,
    updatePaginationControls,
    getCurrentPage,
    getPageSize,
    getTotalPages,
    getTotalItems,
    setCurrentPage,
    setPageSize, 
    setTotalPages,
    setTotalItems
};