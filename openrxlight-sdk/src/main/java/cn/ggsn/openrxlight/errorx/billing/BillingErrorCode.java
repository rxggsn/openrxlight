package cn.ggsn.openrxlight.errorx.billing;

import cn.ggsn.openrxlight.errorx.Constants;
import cn.ggsn.openrxlight.errorx.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum BillingErrorCode implements ErrorCode {
        BillingPackageNotFound(Constants.BILLING_BIZ_CODE + 1,
                        "You are not subscribed to any billing package, please subscribe to a package to continue using the service"),
        InvalidBillingPackage(Constants.BILLING_BIZ_CODE + 2, "Invalid billing package"),
        InsufficientCredit(Constants.BILLING_BIZ_CODE + 3,
                        "Insufficient credit, please upgrade your package or wait for the next billing cycle"),
        CreditPacakgeInfoNotFound(Constants.BILLING_BIZ_CODE + 4, "Credit package information not found"),
        BillingCycleNotSupported(Constants.BILLING_BIZ_CODE + 5, "Billing cycle not supported"),
        UpgradeToLowerPackage(Constants.BILLING_BIZ_CODE + 6,
                        "Cannot upgrade to a package with lower or equal credit capacity"),
        CreditPackageExpired(Constants.BILLING_BIZ_CODE + 7, "Credit package has expired"),
        PermissionDeniedForPayment(Constants.BILLING_BIZ_CODE + 8,
                        "Your organization does not have permission to pay bill. Please contact with your administrator."),
                        ;

        private final int value;
        private final String message;

        public static BillingErrorCode fromValue(Integer code) {
                for (BillingErrorCode errorCode : values()) {
                        if (errorCode.getValue() == code) {
                                return errorCode;
                        }
                }

                return null;
        }
}
