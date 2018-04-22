class Helper {
    constructor() {}

    static fields() {}

    static apply(result) {}

    static fromJson(json) {}

    values() {
        return Array.from(Object.getOwnPropertyNames(this)).map(prop => this[prop]);
    }
}

module.exports = Helper;