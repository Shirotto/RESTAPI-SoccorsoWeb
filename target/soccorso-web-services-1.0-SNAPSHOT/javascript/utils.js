/**
 * utils.js
 * Funzioni di utilità condivise
 */

function formatDate(dateInput) {
    if (!dateInput) return 'N/A';

    try {
        let date;

        if (Array.isArray(dateInput) && dateInput.length >= 6) {
            const [year, month, day, hour, minute, second] = dateInput;
            // Creazione della data in UTC per evitare problemi di fuso orario
            date = new Date(Date.UTC(year, month - 1, day, hour, minute, second || 0));
        }
        else if (typeof dateInput === 'string') {
            const cleanDateString = dateInput.trim();

            const mysqlMatch = cleanDateString.match(/^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})$/);
            if (mysqlMatch) {
                const [, year, month, day, hour, minute, second] = mysqlMatch;
                // Anche qui creiamo la data in UTC se possibile
                date = new Date(Date.UTC(
                    parseInt(year),
                    parseInt(month) - 1,
                    parseInt(day),
                    parseInt(hour),
                    parseInt(minute),
                    parseInt(second)
                ));
            } else {
                date = new Date(cleanDateString);
            }
        }
        else if (typeof dateInput === 'number') {
            date = new Date(dateInput);
        }
        else {
            return 'N/A';
        }

        if (isNaN(date.getTime())) {
            return 'N/A';
        }

        const formatted = date.toLocaleString('it-IT', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            timeZone: 'UTC'  // Visualizza in orario UTC per evitare il +2
        });

        return formatted;

    } catch (error) {
        return 'N/A';
    }
}

function arrayToDateString(arr) {
    if (!Array.isArray(arr) || arr.length < 6) return null;
    const [year, month, day, hour, min, sec] = arr;
    return `${year}-${String(month).padStart(2,'0')}-${String(day).padStart(2,'0')} ${String(hour).padStart(2,'0')}:${String(min).padStart(2,'0')}:${String(sec).padStart(2,'0')}`;
}

// Esportiamo le funzioni come oggetto
const Utils = {
    formatDate,
    arrayToDateString
};