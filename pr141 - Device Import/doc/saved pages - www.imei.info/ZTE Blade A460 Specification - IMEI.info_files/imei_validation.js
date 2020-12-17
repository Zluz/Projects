// Tools for validating IMEI number and/or IMEI form input
//
// USAGE:
// - IMEIValidator.cleanIMEI(imei)
// - IMEIValidator.generateCD(imei14)
// - IMEIValidator.validateCD(imei)
// - IMEIValidator.validateIMEI(imei)
// - IMEIValidator.validateInput(imeiInput)


var IMEIValidator = (function() {

  const cleanIMEI = function (imei) {
    // Remove invalid characters from specified IMEI number

    return imei.replace(/[^0-9]/g, '');
  };

  const generateCD = function (imei14) {
    // Generate check digit for specified IMEI number (14 digits)

    let sumA = 0;
    let sumB = 0;

    for (let i = 0; i < imei14.length; i = i + 2) {
      sumB += parseInt(imei14[i]);

      const x = (2 * parseInt(imei14[i + 1])).toString();

      for (let j = 0; j < x.length; j++) {
        sumA += parseInt(x[j]);
      }

    }

    let result = (sumA + sumB) % 10;

    if (result !== 0) {
      result = 10 - result;
    }

    return result;
  };

  const validateCD = function (imei) {
    // Validate check digit for specified IMEI number (15 digits)

    const imei14 = imei.slice(0, imei.length - 1);

    return generateCD(imei14) === parseInt(imei[imei.length - 1])
  }

  const validateIMEI = function (imei) {
    // Validate specified IMEI number (15 digits)

    return Boolean(imei.match(/^[0-9]{15}$/)) && validateCD(imei);

  }

  const setInputValid = function (imeiInput) {
    imeiInput.setCustomValidity('');
  };

  const setInputInvalid = function (imeiInput) {
    imeiInput.setCustomValidity('Invalid!');
  };

  const validateInput = function (imeiInput) {
    // Validate specified IMEI form input (15 digits IMEI)
    // Set custom validity on input element if IMEI invalid

    const imei = cleanIMEI(imeiInput.value);

    imeiInput.value = imei;

    if (validateIMEI(imei)) {
        // IMEI valid
        setInputValid(imeiInput);
        return true;
    }

    // IMEI invalid
    setInputInvalid(imeiInput);
    return false;
  };

  return {
    cleanIMEI: cleanIMEI,
    generateCD: generateCD,
    validateCD: validateCD,
    validateIMEI: validateIMEI,
    setInputValid: setInputValid,
    setInputInvalid: setInputInvalid,
    validateInput: validateInput
  }

})();
