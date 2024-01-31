import React, { Component } from 'react';

import { formatNumberString } from 'scalajs:utils.js'

import './display.css'

export default class Display extends Component {
  render() {
    const sign = this.props.positive ? '' : '-'

    return (
      <div className='component-display'>
        <div className='operation-view'>
          {this.props.operation}
        </div>
        <div className='number-view'>
          <span className='component-display-span'>
              {sign}{formatNumberString(this.props.value)}
          </span>
        </div>
      </div>
    );
  }
}