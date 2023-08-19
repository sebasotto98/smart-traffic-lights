import React from 'react';
import './DropDown.css';

const DropDown = (props) => {
  return (
    <div className='dropdown'>
      <div className='button'>{props.dropdownName}</div>
      <div className='dropdown-content'>{props.children}</div>
    </div>
  );
};

export default DropDown;
