# Manual Testing and Verification Guide

This guide details the steps to launch the IPCMS application and verify all functional requirements (FR1 - FR7).

---

## 1. Startup Instructions

### Prerequisites
* Ensure PostgreSQL is running on `localhost:5432`.
* Create the database named `insurance_db` (if it does not exist):
  ```sql
  CREATE DATABASE insurance_db;
  ```
  *(Credentials are configured in `application.properties` as username `postgres` and password `Anchal@18`)*.

### Command Execution

Open two terminal windows:

#### Terminal 1 (Backend)
```powershell
cd e:\Insurance-policy-claim-system\backend
mvn spring-boot:run
```
*(Runs on port `8080`)*

#### Terminal 2 (Frontend)
```powershell
cd e:\Insurance-policy-claim-system\frontend
npm run dev
```
*(Runs on port `5173` - open [http://localhost:5173](http://localhost:5173) in your browser)*

---

## 2. Seeded Accounts / Credentials

Use these seeded logins to test the respective role workflows (all accounts use password: `password123`):

* **System Administrator (Admin)**: `admin@ipcms.local`
* **Underwriter**: `underwriter@ipcms.local`
* **Claims Handler**: `claims_handler@ipcms.local`
* **Insurance Agent**: `agent@ipcms.local`
* **Standard Policyholder**: `holder@ipcms.local`

---

## 3. Step-by-Step Functional Verification

### Scenario A: JWT Authentication & User Onboarding (FR1 & FR7)
1. Navigate to the login screen at `http://localhost:5173`.
2. Select any role from the dropdown, fill in the email (e.g., `admin@ipcms.local`), enter password `password123`, and click **Login**.
3. Verify that the correct dashboard layout loads and sidebar items filter appropriately according to the logged-in role.
4. Click **Log out** in the sidebar.

### Scenario B: Asynchronous AML Blacklist Lock (FR5)
1. On the login screen, click **Register / Onboard Policyholder**.
2. Fill out the form fields. To trigger an AML blacklist lockout, enter:
   * **Legal Name**: `Sanctioned Individual` (or `Terrorist Organization`)
   * **Tax ID**: `TAX-ILLEGAL-999` (or `OFAC-BAD-777`)
3. Click **Register**.
4. The registration will succeed initially and return a `PENDING` status.
5. In the background (within 1 second), the async AML service matches the credentials and flags the user as `FLAGGED` while setting `isActive = false` in the database.
6. Attempt to log in with this newly registered account's email and password.
7. Verify that login is rejected with an **"Authentication failed"** (or disabled account) error.

### Scenario C: Underwriter Matrix & Quote Ratings (FR2)
1. Log in as `underwriter@ipcms.local` (password: `password123`).
2. Go to the **Underwriting Matrix** tab.
3. Adjust the four sliders (Region, Asset Age, Prior Claims, Business Type) to various values.
4. Click **Generate Premium Quote**.
   * Verify that the composite score is calculated as `average of sliders`.
   * Verify that the premium quote is calculated as `500.00 + (15.00 * compositeScore)`.
5. Enter a policy number (e.g., `POL-2024-001`), select an override type (e.g., `"Premium Rate Reduction"`), enter delta (e.g., `"-10%"`), and click **Submit Override**.
6. Log out and log back in as `admin@ipcms.local` (password: `password123`).
7. Go to **Underwriting Matrix**, locate the pending override request, and click **Approve** or **Reject**. Verify the status changes.

### Scenario D: Claims Registration & FNOL (FR3)
1. Log in as `agent@ipcms.local` or `holder@ipcms.local`.
2. Go to **Claims Management** -> click **File FNOL**.
3. Fill out the form. 
   * **Verification 1 (Fail)**: Select an **Incident Date** that is older than 14 days (e.g., 20 days ago) and click **Register FNOL**. Verify that a validation warning alerts that the reporting window has been exceeded.
   * **Verification 2 (Success)**: Select a date within 14 days (e.g., 3 days ago) and register. The claim should register, generating a dynamic `CLM-YYYY-NNNN` number.

### Scenario E: Claims Reserves Modification (FR3)
1. Log in as `claims_handler@ipcms.local`.
2. Go to **Claims Management** -> **All Claims** list.
3. Locate a claim (the reserve value is initialized to `$0.00` by default).
4. Click inside the **Gross Reserve** number input field in the table row.
5. Edit the reserve value (e.g., enter `15000`) and click outside the input field (triggering the blur event).
6. Verify that the table refreshes showing the saved value, and status updates from `"Pending FNOL"` to `"Under Investigation"`.

### Scenario F: Policy Endorsements & Archiving (FR4, FR6)
1. Log in as `underwriter@ipcms.local`.
2. Go to the **Policy Register** tab.
3. Locate an active policy (e.g., `POL-2024-001`), and click the **Endorse** button.
4. Modify the product type or base premium.
   * Verify that the pro-rata projection details update in real-time as you enter a new premium.
5. Click **Apply Endorsement**.
6. The policy premium updates. Now click the **History** button on that same policy row.
7. Verify that the snapshots log opens, displaying the archived baseline state with the previous product type and base premium values.

### Scenario G: Audit Trails (FR6)
1. Log in as `admin@ipcms.local`.
2. Go to **Administration** -> click the **Audit Log** tab.
3. Verify that the logs table displays chronological footprints of all actions: onboarding events, override allocations, claims registrations, reserve modifications, and policy endorsements, complete with the user email and details.
