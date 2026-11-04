"""Loan-service parity scenarios. Data comes from data-legacy.sql loaded at startup."""


def loans_list(c, s):
    c.call("GET", "/api/loans")


def loan_detail(c, s):
    for loan_id in ["LN-2019-00142", "LN-2018-00089", "LN-2021-00567"]:
        c.call("GET", "/api/loans/" + loan_id)


def loan_not_found(c, s):
    c.call("GET", "/api/loans/LN-0000-00000")


def loan_malformed_id(c, s):
    c.call("GET", "/api/loans/%20", raw_path=True)
    c.call("GET", "/api/loans/LN-2019-00142%2Fx", raw_path=True)
    c.call("GET", "/api/loans/" + "9" * 300)


def borrowers_list(c, s):
    c.call("GET", "/api/borrowers")


def borrower_detail(c, s):
    for borrower_id in ["B-10001", "B-10002", "B-10005"]:
        c.call("GET", "/api/borrowers/" + borrower_id)


def borrower_not_found(c, s):
    c.call("GET", "/api/borrowers/B-99999")


def borrower_malformed_id(c, s):
    c.call("GET", "/api/borrowers/%20", raw_path=True)
    c.call("GET", "/api/borrowers/B-10001%2F..", raw_path=True)


def loan_payments(c, s):
    for loan_id in ["LN-2019-00142", "LN-2018-00089"]:
        c.call("GET", "/api/loans/%s/payments" % loan_id)


def loan_payments_unknown_loan(c, s):
    c.call("GET", "/api/loans/LN-0000-00000/payments")
    c.call("GET", "/api/loans/not-a-loan/payments")


def borrower_exposure(c, s):
    for borrower_id in ["B-10001", "B-10002", "B-10003", "B-10004", "B-10005"]:
        c.call("GET", "/api/borrowers/%s/exposure" % borrower_id)


def borrower_exposure_not_found(c, s):
    c.call("GET", "/api/borrowers/B-99999/exposure")


def unknown_route(c, s):
    c.call("GET", "/api/payments/loan/LN-2019-00142")


SCENARIOS = [
    ("loans-list", loans_list),
    ("loan-detail", loan_detail),
    ("loan-not-found", loan_not_found),
    ("loan-malformed-id", loan_malformed_id),
    ("borrowers-list", borrowers_list),
    ("borrower-detail", borrower_detail),
    ("borrower-not-found", borrower_not_found),
    ("borrower-malformed-id", borrower_malformed_id),
    ("loan-payments", loan_payments),
    ("loan-payments-unknown-loan", loan_payments_unknown_loan),
    ("borrower-exposure", borrower_exposure),
    ("borrower-exposure-not-found", borrower_exposure_not_found),
    ("unknown-route", unknown_route),
]
