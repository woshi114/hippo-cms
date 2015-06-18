package org.hippoecm.frontend.plugins.cms.admin.users;

import org.apache.wicket.extensions.validation.validator.RfcCompliantEmailAddressValidator;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.junit.Test;

import junit.framework.Assert;

/**
 */
public class TestCreateUserPanel {

    /* CMS7-9148 */
    @Test
    public void testEmailValidation(){

        String email = "o'brian-01_01@e-mail-01.com";

        IValidatable<String> emailValidatable = new EmailValidatable(email);
        EmailAddressValidator emailAddressValidator = EmailAddressValidator.getInstance();
        emailAddressValidator.validate(emailValidatable);
        Assert.assertFalse(emailValidatable.isValid());

        emailValidatable = new EmailValidatable(email);
        RfcCompliantEmailAddressValidator rfcCompliantEmailAddressValidator = RfcCompliantEmailAddressValidator.getInstance();
        rfcCompliantEmailAddressValidator.validate(emailValidatable);
        Assert.assertTrue(emailValidatable.isValid());
    }

    private class EmailValidatable implements IValidatable<String> {

        private boolean valid = true;
        private final String email;

        public EmailValidatable(final String email){
            this.email = email;
        }

        @Override
        public String getValue() {
            return this.email;
        }

        @Override
        public void error(final IValidationError error) {
            System.out.println("Validation error: "+error.toString());
            this.valid = false;
        }

        @Override
        public boolean isValid() {
            return this.valid;
        }

        @Override
        public IModel<String> getModel() {
            return null;
        }
    }
}
