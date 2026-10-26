from pathlib import Path
import subprocess, sqlite3, json, tempfile
from contextlib import closing
adb='C:/Users/ANT-PC/AppData/Local/Android/Sdk/platform-tools/adb.exe'
with tempfile.TemporaryDirectory() as directory:
    target=Path(directory)/'healers.db'
    for suffix in ['', '-wal', '-shm']:
        result=subprocess.run([adb,'-s','RZCX21YBGRK','exec-out','run-as','com.healersjourney.app','cat','databases/healers.db'+suffix],capture_output=True,check=True)
        (Path(str(target)+suffix)).write_bytes(result.stdout)
    with closing(sqlite3.connect(target)) as db:
        version=db.execute('pragma user_version').fetchone()[0]
        print('Database version:',version)
        assert version==2, 'Database migration has not run'
        extras=json.loads(db.execute('select gameExtrasJson from profile').fetchone()[0])
        print('Unlocked collection items:',len(extras.get('worldUnlocks',[])))
        print('Persisted care routines:',len(extras.get('careRoutines',[])))
        assert len(extras.get('worldUnlocks',[]))>=30,'Test unlocks not persisted'
        assert 'careRoutines' in extras,'Care settings not persisted'
    print('PASS: installed database migration and test unlock persistence')
