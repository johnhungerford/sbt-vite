import React, { Component } from 'react';
import PropTypes from 'prop-types';

import './button.css'

export default class Button extends Component {
    handleClick = () => {
        this.props.clickHandler(this.props.name);
    }

    render() {
        if (this.props.name === 'spacer') {
            return (
                <div className={'component-button dark'}>
                    <div className='button-text invisible-elem'>
                        <span>xxx</span>
                    </div>
                </div>
            );
        }

        return (
            <div className={'component-button'} onClick={this.handleClick}>
                <div className='button-text'>
                    <span>{this.props.name}</span>
                </div>
            </div>
        );
    }
}

Button.propTypes = {
    clickHandler: PropTypes.func,
    name: PropTypes.string,
}