import { stringify } from "querystring";

/**
 * Adds commas to a string-encoded number as appropriate. Detects decimals. Does not
 * mutate original string.
 * @param {string} strin number encoded in a string
 * @returns {string} A version of strin with commas added as appropriate
 */
export default function addCommas(strin) {
    var strout = strin.slice();
    // Return original (copy of) if too small to have comma
    if (strout.length < 4) return strout;

    // Look for decimal point
    for (let i = strout.length - 1; i >= 0; i--) {
        if (strout[i] === '.') {
            // Climb down string by three adding commas until beginning is reached
            for(let j = i - 3; j > 0; j -= 3) {
                strout = splice(strout, j ,0,',');
            }

            return strout;
        }
    }

    // If no decimal, climb down string from the end by three, adding comma until beginning
    // is reached.
    for (let i = strout.length - 3; i > 0; i -= 3) strout = splice(strout, i, 0,',');
    return strout;
}

// Like Array.splice(), but for strings; doesn't mutate (for some reason!!)
function splice(str, i, del, ins) {
    if (i >= str.length || i < 0) return this;
    const begin = str.slice(0,i);
    const end = str.slice(i + del);
    str = begin + ins + end;
    return str;
}
