class AppError extends Error {
  constructor(message, statusCode = 400) {
    super(message);
    this.name = "AppError";
    this.statusCode = statusCode;
  }
}

function isAppError(error) {
  return error instanceof AppError;
}

module.exports = { AppError, isAppError };
