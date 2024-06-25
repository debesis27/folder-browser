class StateManager {
  constructor() {
    if(!StateManager.instance) {
      this._state = {
        rootFolder: null,
        currentFolder: null,
        selectedFileSystemItem: null,
        selectedFileSystemItemElement: null,
        selectedDestinationFolder: null,
      };
      StateManager.instance = this;
    }
    return StateManager.instance;
  }

  getState() {
    return this._state;
  }

  setState(state) {
    this._state = {...this._state, ...state};
  }
}

const instance = new StateManager();

export default instance;
