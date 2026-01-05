buildPlugin(
    useContainerAgent: true,
    tests: [skip: true],
    skipTests: true,
    configurations: [
        [platform: 'linux', jdk: 17],
        [platform: 'windows', jdk: 17]
    ],
    spotbugs: [
        qualityGates: [
            [
                threshold: 1000,
                type: 'TOTAL',
                unstable: false
            ]
        ]
    ]
)
