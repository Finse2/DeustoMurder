
# DeustoMurder - Contributor statistics
# Excludes XML and compiled CLASS files

$stats = @{}
$author = $null

$history = git log HEAD --use-mailmap --numstat `
    --format="AUTHOR:%aN <%aE>" `
    -- . ':(exclude,glob)**/*.xml' ':(exclude,glob)**/*.class'

foreach ($line in $history) {

    if ($line -match '^AUTHOR:(.+)$') {
        $author = $Matches[1]

        if (-not $stats.ContainsKey($author)) {
            $stats[$author] = @{
                Added = 0
                Deleted = 0
            }
        }
        continue
    }

    if ($null -eq $author) { continue }

    if ($line -match '^(\d+)\s+(\d+)\s+(.+)$') {
        $stats[$author].Added += [int]$Matches[1]
        $stats[$author].Deleted += [int]$Matches[2]
    }
}

$results = foreach ($name in $stats.Keys) {
    $added = $stats[$name].Added
    $deleted = $stats[$name].Deleted

    [PSCustomObject]@{
        Contributor = $name
        Added = $added
        Deleted = $deleted
        Net = $added - $deleted
    }
}

$results |
    Sort-Object Net -Descending |
    Format-Table -AutoSize
