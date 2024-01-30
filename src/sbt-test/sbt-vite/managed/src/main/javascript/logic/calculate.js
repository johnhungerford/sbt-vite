/**
 * A function to calculate a new calculator state based on the current state and a button
 * name. The calculator state tracks the current total, the next value to be operated on, 
 * and the operation to perform. calculate() either performs the operation (stored in state.total),
 * registers the operation to be performed (when a new state.next is provided), or fills in
 * state.next with another digit.
 * @param {object} currentState     Calculator state. Properties are "total" (string encoded
 *                                  number), "next" (string encoded number), and "operation"
 *                                  (string), which can be 'x', '÷', '+', or '-'.
 * @param {string} buttonName       Can be '.', '0', '1', '2', '3', '4', '5', '6' ,'7' ,'8', '9',
 *                                  'x', '÷', '=', '+/-', '+', or '-'.
 * @returns {object} New state object for React component "App" (see App.js)
 */
export default function calculate(currentState, buttonName) {
    // Validate parameters
    if (buttonName === undefined) throw new TypeError('calculate(): buttonName parameter invalid');
    if (currentState.total === undefined || currentState.next === undefined || currentState.operation === undefined) {
        throw new TypeError('calculate(): currentState parameter missing or invalid');
    }

    // Trivial cases
    if (buttonName === 'empty') return currentState;
    if (buttonName === 'AC') return { total: null, next: null, operation: null };

    // Perform operation when '=' is pressed: put new total in state.total; make
    // state.new and state.operation null
    if (buttonName === '=') {
        if (currentState.total === null) return currentState;
        if (currentState.next === null) return currentState;
        if (currentState.operation === null) return currentState;

        // string-encoded numbers are converted to floating point numbers
        let newTotal = parseFloat(currentState.total);

        switch(currentState.operation) {
            case 'x':
                newTotal *= parseFloat(currentState.next);
                break;
            case '-':
                newTotal -= parseFloat(currentState.next);
                break;
            case '+':
                newTotal += parseFloat(currentState.next);
                break;
            case '÷':
                newTotal /= parseFloat(currentState.next);
                break;
            default:
                throw new Error('Invalid state.operation!');
        }

        return {
            total: null,
            next: newTotal.toString(),
            operation: null,
        };
    }

    // '+/-' just changes sign of state.next (which is displayed)
    if (buttonName === '+/-') {
        if (currentState.next === null || parseFloat(currentState.next === '0') === 0) return currentState;
        if (currentState.next[0] === '-') {
            var newNext = currentState.next.slice(1);
        } else {
            var newNext = '-' + currentState.next;
        }

        return {
            total: currentState.total,
            next: newNext.toString(),
            operation: currentState.operation,
        };
    }

    // Handle all other buttons: numbers and operations
    switch(buttonName) {
        case 'x':
        case '-':
        case '+':
        case '÷':
            // if no total, put state.next in state.total, and put button in state.operation
            if (currentState.total === null) {
                if (currentState.next === null) return currentState;
                return {
                    total: currentState.next,
                    next: null,
                    operation: buttonName,
                };
            }

            // otherwise, perform call calculate with '=' button, which will either perform
            // the state.operation on state.total and state.next, or, if there is no state.next,
            // it will do nothing.
            currentState = calculate(currentState, '=');
            // put buttonName in state.operation
            return {
                total: currentState.total,
                next: currentState.next,
                operation: buttonName,
            }
            break;
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            // If no state.next, state.next becomes buttonName for numbers
            if (currentState.next === null) {
                return {
                    total: currentState.total,
                    next: buttonName,
                    operation: currentState.operation,
                };
            }

            // Handle when there is a zero (replace zero, don't append)
            if (currentState.next === '0') {
                return {
                    total: currentState.total,
                    next: buttonName,
                    operation: currentState.operation,
                };
            }

            // If state.total is null, state.next is not, and state.operation is loaded,
            // this means state.next needs to be switched to state.total, and a new state.next
            // begun.
            if (currentState.total === null && currentState.operation !== null) {
                return {
                    total: currentState.next,
                    next: buttonName,
                    operation: currentState.operation,
                } 
            }
            
            // For all other values of state.next, append buttonName (digit)
            return {
                total: currentState.total,
                next: currentState.next + buttonName,
                operation: currentState.operation,
            } 
            break;
        case '.':
            // '.' is just like a number, but has to check to see if there are already any
            // periods in state.next or not; also inserts '0.' if it is the first digit.
            if (currentState.next === null || parseFloat(currentState.next) === 0) {
                return {
                    total: currentState.total,
                    next: '0.',
                    operation: currentState.operation,
                };
            }

            if (currentState.total === null && currentState.operation !== null) {
                return {
                    total: currentState.next,
                    next: '0.',
                    operation: currentState.operation,
                }
            }
            
            for (let i = 0; i < currentState.next.length; i++) {
                if (currentState.next[i] === '.') return currentState;
            }

            return {
                total: currentState.total,
                next: currentState.next + '.',
                operation: currentState.operation,
            }
        default:
            // There shouldn't be any button names submitted to this function outside of
            // the above.
            throw new Error('Invalid buttonName!');
    }
}
