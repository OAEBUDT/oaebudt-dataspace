# /// script
# requires-python = ">=3.8"
# dependencies = [
#     "requests",
# ]
# ///

if __name__ == '__main__':
    import sys
    print('\nPlease do not run this script directly.\n')
    sys.exit(1)


from os import getenv

from oaebudt_dataspace import DataspaceClient


# These constants can be set in the environment or hard-coded if preferred.
DOMAIN_NAME = getenv('DOMAIN_NAME', '<domain-name>')
REALM_NAME = getenv('REALM_NAME', '<realm-name>')
CLIENT_ID = getenv('CLIENT_ID', '<client-id>')
CLIENT_SECRET = getenv('CLIENT_SECRET', '<client-secret>')
USERNAME = getenv('USERNAME', '<username>')
PASSWORD = getenv('PASSWORD', '<password>')


# 1. Authentication and Token Retrieval

# Initialise the client
client = DataspaceClient(
    domain_name=DOMAIN_NAME,
    realm_name=REALM_NAME,
    client_id=CLIENT_ID,
    client_secret=CLIENT_SECRET,
    username=USERNAME,
    password=PASSWORD,
)
# The client automatically fetches the token, used for interacting
# with the API, and sets it in the request headers.


# 2. Report Publishing
# 2.1. Participant Groups Management

# 2.1.1. Create Participant Group
# Please change the group_name and participants as required.
response = client.create_participant_group(
    group_name='<group-name>',
    participants=[
        "did:web:participant1-domain-name",
        "did:web:participant2-domain-name",
        "did:web:participant3-domain-name"
    ],
)

print(response.json())
# If the response was successful, you should be able to see the following
# -> {'message': 'Participants added to group'}


# 2.1.2. Retrieve Participant Group
participant_groups = client.retrieve_participant_groups()
print(participant_groups)
"""# -> 
[
  {
    "id": "<group-name>",
    "participants": [
      "did:web:participant3-domain-name",
      "did:web:participant2-domain-name",
      "did:web:participant1-domain-name"
    ]
  }
]
"""

# 2.2. Asset Creation
# Again, please change the variables, as needed.

# The usage report file (must be in JSON format)
file_to_upload = '/path/to/your/local/file/report.json'

# Descriptive title for the report
title = "ITEM Report Publisher XYZ 1st semester"

# Additional report information
metadata = {
    'legalOrganizationName': 'University XYZ',
    'countryOfOrganization': 'United States',
    'organizationWebsite': 'https://www.example-press.org',
    'contactPerson': 'Jane Smith',
    'contactEmail': 'jane.smith@example-press.org',
    'dataProcessingDescription': 'Raw usage logs are processed using COUNTER Release 5 processing rules...',
    'qualityAssuranceMeasures': 'Monthly data validation process including outlier detection, completeness checking...',
    'dataLicensingTerms': 'Data is provided under CC-BY license...',
    'dataAccuracyLevel': 3,
    'dataGenerationTransparencyLevel': 2,
    'dataDeliveryReliabilityLevel': 3,
    'dataFrequencyLevel': 2,
    'dataGranularityLevel': 2,
    'dataConsistencyLevel': 2
}

# Optional: Report category: "ITEM_REPORT" or "TITLE_REPORT"
report_category = "ITEM_REPORT"

# Name of the Participant Group allowed to access the report.
access_group = "<group-name>"


# API Request to Upload Report File
response = client.create_file_based_report(
    file_path=file_to_upload,
    asset_metadata=metadata,
    report_title=title,
    target_audience=access_group,
    report_type=report_category,
)

print(response.json())
""" # ->
{
    'message': 'Asset created successfully',
    'assetId': 'ITEM_REPORT-e129824x-qc21-137z-94bq-12bd52f8a62p'
}
"""
