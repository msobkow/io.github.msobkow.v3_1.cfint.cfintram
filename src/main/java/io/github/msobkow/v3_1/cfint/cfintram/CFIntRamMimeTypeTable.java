
// Description: Java 25 in-memory RAM DbIO implementation for MimeType.

/*
 *	io.github.msobkow.CFInt
 *
 *	Copyright (c) 2016-2026 Mark Stephen Sobkow
 *	
 *	Mark's Code Fractal 3.1 CFInt - Internet Essentials
 *	
 *	This file is part of Mark's Code Fractal CFInt.
 *	
 *	Mark's Code Fractal CFInt is available under dual commercial license from
 *	Mark Stephen Sobkow, or under the terms of the GNU Library General Public License,
 *	Version 3 or later.
 *	
 *	Mark's Code Fractal CFInt is free software: you can redistribute it and/or
 *	modify it under the terms of the GNU Library General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *	
 *	Mark's Code Fractal CFInt is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *	
 *	You should have received a copy of the GNU Library General Public License
 *	along with Mark's Code Fractal CFInt.  If not, see <https://www.gnu.org/licenses/>.
 *	
 *	If you wish to modify and use this code without publishing your changes in order to
 *	tie it to proprietary code, please contact Mark Stephen Sobkow
 *	for a commercial license at mark.sobkow@gmail.com
 *	
 */

package io.github.msobkow.v3_1.cfint.cfintram;

import java.math.*;
import java.sql.*;
import java.text.*;
import java.time.*;
import java.util.*;
import org.apache.commons.codec.binary.Base64;
import io.github.msobkow.v3_1.cflib.*;
import io.github.msobkow.v3_1.cflib.dbutil.*;

import io.github.msobkow.v3_1.cfsec.cfsec.*;
import io.github.msobkow.v3_1.cfint.cfint.*;
import io.github.msobkow.v3_1.cfsec.cfsec.buff.*;
import io.github.msobkow.v3_1.cfint.cfint.buff.*;
import io.github.msobkow.v3_1.cfsec.cfsecobj.*;
import io.github.msobkow.v3_1.cfint.cfintobj.*;

/*
 *	CFIntRamMimeTypeTable in-memory RAM DbIO implementation
 *	for MimeType.
 */
public class CFIntRamMimeTypeTable
	implements ICFIntMimeTypeTable
{
	private ICFIntSchema schema;
	private Map< Integer,
				CFIntBuffMimeType > dictByPKey
		= new HashMap< Integer,
				CFIntBuffMimeType >();
	private Map< CFIntBuffMimeTypeByUNameIdxKey,
			CFIntBuffMimeType > dictByUNameIdx
		= new HashMap< CFIntBuffMimeTypeByUNameIdxKey,
			CFIntBuffMimeType >();

	public CFIntRamMimeTypeTable( ICFIntSchema argSchema ) {
		schema = argSchema;
	}

	public CFIntBuffMimeType ensureRec(ICFIntMimeType rec) {
		if (rec == null) {
			return( null );
		}
		else {
			int classCode = rec.getClassCode();
			if (classCode == ICFIntMimeType.CLASS_CODE) {
				return( ((CFIntBuffMimeTypeDefaultFactory)(schema.getFactoryMimeType())).ensureRec(rec) );
			}
			else {
				throw new CFLibUnsupportedClassException(getClass(), "ensureRec", 1, "rec", "Not " + Integer.toString(classCode));
			}
		}
	}

	public ICFIntMimeType createMimeType( ICFSecAuthorization Authorization,
		ICFIntMimeType iBuff )
	{
		final String S_ProcName = "createMimeType";
		
		CFIntBuffMimeType Buff = ensureRec(iBuff);
		Integer pkey;
		pkey = schema.nextMimeTypeIdGen();
		Buff.setRequiredMimeTypeId( pkey );
		CFIntBuffMimeTypeByUNameIdxKey keyUNameIdx = (CFIntBuffMimeTypeByUNameIdxKey)schema.getFactoryMimeType().newByUNameIdxKey();
		keyUNameIdx.setRequiredName( Buff.getRequiredName() );

		// Validate unique indexes

		if( dictByPKey.containsKey( pkey ) ) {
			throw new CFLibPrimaryKeyNotNewException( getClass(), S_ProcName, pkey );
		}

		if( dictByUNameIdx.containsKey( keyUNameIdx ) ) {
			throw new CFLibUniqueIndexViolationException( getClass(),
				S_ProcName,
				"MimeTypeUNameIdx",
				"MimeTypeUNameIdx",
				keyUNameIdx );
		}

		// Validate foreign keys

		// Proceed with adding the new record

		dictByPKey.put( pkey, Buff );

		dictByUNameIdx.put( keyUNameIdx, Buff );

		if (Buff == null) {
			return( null );
		}
		else {
			int classCode = Buff.getClassCode();
			if (classCode == ICFIntMimeType.CLASS_CODE) {
				CFIntBuffMimeType retbuff = ((CFIntBuffMimeType)(schema.getFactoryMimeType().newRec()));
				retbuff.set(Buff);
				return( retbuff );
			}
			else {
				throw new CFLibUnsupportedClassException(getClass(), S_ProcName, 0, "-create-buff-cloning-", "Not " + Integer.toString(classCode));
			}
		}
	}

	public ICFIntMimeType readDerived( ICFSecAuthorization Authorization,
		Integer PKey )
	{
		final String S_ProcName = "CFIntRamMimeType.readDerived";
		ICFIntMimeType buff;
		if( dictByPKey.containsKey( PKey ) ) {
			buff = dictByPKey.get( PKey );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public ICFIntMimeType lockDerived( ICFSecAuthorization Authorization,
		Integer PKey )
	{
		final String S_ProcName = "CFIntRamMimeType.readDerived";
		ICFIntMimeType buff;
		if( dictByPKey.containsKey( PKey ) ) {
			buff = dictByPKey.get( PKey );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public ICFIntMimeType[] readAllDerived( ICFSecAuthorization Authorization ) {
		final String S_ProcName = "CFIntRamMimeType.readAllDerived";
		ICFIntMimeType[] retList = new ICFIntMimeType[ dictByPKey.values().size() ];
		Iterator< ICFIntMimeType > iter = dictByPKey.values().iterator();
		int idx = 0;
		while( iter.hasNext() ) {
			retList[ idx++ ] = iter.next();
		}
		return( retList );
	}

	public ICFIntMimeType readDerivedByUNameIdx( ICFSecAuthorization Authorization,
		String Name )
	{
		final String S_ProcName = "CFIntRamMimeType.readDerivedByUNameIdx";
		CFIntBuffMimeTypeByUNameIdxKey key = (CFIntBuffMimeTypeByUNameIdxKey)schema.getFactoryMimeType().newByUNameIdxKey();
		key.setRequiredName( Name );

		ICFIntMimeType buff;
		if( dictByUNameIdx.containsKey( key ) ) {
			buff = dictByUNameIdx.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public ICFIntMimeType readDerivedByIdIdx( ICFSecAuthorization Authorization,
		int MimeTypeId )
	{
		final String S_ProcName = "CFIntRamMimeType.readDerivedByIdIdx() ";
		ICFIntMimeType buff;
		if( dictByPKey.containsKey( MimeTypeId ) ) {
			buff = dictByPKey.get( MimeTypeId );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public ICFIntMimeType readBuff( ICFSecAuthorization Authorization,
		Integer PKey )
	{
		final String S_ProcName = "CFIntRamMimeType.readBuff";
		ICFIntMimeType buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( buff.getClassCode() != ICFIntMimeType.CLASS_CODE ) ) {
			buff = null;
		}
		return( buff );
	}

	public ICFIntMimeType lockBuff( ICFSecAuthorization Authorization,
		Integer PKey )
	{
		final String S_ProcName = "lockBuff";
		ICFIntMimeType buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( buff.getClassCode() != ICFIntMimeType.CLASS_CODE ) ) {
			buff = null;
		}
		return( buff );
	}

	public ICFIntMimeType[] readAllBuff( ICFSecAuthorization Authorization )
	{
		final String S_ProcName = "CFIntRamMimeType.readAllBuff";
		ICFIntMimeType buff;
		ArrayList<ICFIntMimeType> filteredList = new ArrayList<ICFIntMimeType>();
		ICFIntMimeType[] buffList = readAllDerived( Authorization );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && ( buff.getClassCode() == ICFIntMimeType.CLASS_CODE ) ) {
				filteredList.add( buff );
			}
		}
		return( filteredList.toArray( new ICFIntMimeType[0] ) );
	}

	public ICFIntMimeType readBuffByIdIdx( ICFSecAuthorization Authorization,
		int MimeTypeId )
	{
		final String S_ProcName = "CFIntRamMimeType.readBuffByIdIdx() ";
		ICFIntMimeType buff = readDerivedByIdIdx( Authorization,
			MimeTypeId );
		if( ( buff != null ) && ( buff.getClassCode() == ICFIntMimeType.CLASS_CODE ) ) {
			return( (ICFIntMimeType)buff );
		}
		else {
			return( null );
		}
	}

	public ICFIntMimeType readBuffByUNameIdx( ICFSecAuthorization Authorization,
		String Name )
	{
		final String S_ProcName = "CFIntRamMimeType.readBuffByUNameIdx() ";
		ICFIntMimeType buff = readDerivedByUNameIdx( Authorization,
			Name );
		if( ( buff != null ) && ( buff.getClassCode() == ICFIntMimeType.CLASS_CODE ) ) {
			return( (ICFIntMimeType)buff );
		}
		else {
			return( null );
		}
	}

	public ICFIntMimeType updateMimeType( ICFSecAuthorization Authorization,
		ICFIntMimeType Buff )
	{
		Integer pkey = Buff.getPKey();
		ICFIntMimeType existing = dictByPKey.get( pkey );
		if( existing == null ) {
			throw new CFLibStaleCacheDetectedException( getClass(),
				"updateMimeType",
				"Existing record not found",
				"MimeType",
				pkey );
		}
		if( existing.getRequiredRevision() != Buff.getRequiredRevision() ) {
			throw new CFLibCollisionDetectedException( getClass(),
				"updateMimeType",
				pkey );
		}
		Buff.setRequiredRevision( Buff.getRequiredRevision() + 1 );
		CFIntBuffMimeTypeByUNameIdxKey existingKeyUNameIdx = (CFIntBuffMimeTypeByUNameIdxKey)schema.getFactoryMimeType().newByUNameIdxKey();
		existingKeyUNameIdx.setRequiredName( existing.getRequiredName() );

		CFIntBuffMimeTypeByUNameIdxKey newKeyUNameIdx = (CFIntBuffMimeTypeByUNameIdxKey)schema.getFactoryMimeType().newByUNameIdxKey();
		newKeyUNameIdx.setRequiredName( Buff.getRequiredName() );

		// Check unique indexes

		if( ! existingKeyUNameIdx.equals( newKeyUNameIdx ) ) {
			if( dictByUNameIdx.containsKey( newKeyUNameIdx ) ) {
				throw new CFLibUniqueIndexViolationException( getClass(),
					"updateMimeType",
					"MimeTypeUNameIdx",
					"MimeTypeUNameIdx",
					newKeyUNameIdx );
			}
		}

		// Validate foreign keys

		// Update is valid

		Map< Integer, CFIntBuffMimeType > subdict;

		dictByPKey.remove( pkey );
		dictByPKey.put( pkey, Buff );

		dictByUNameIdx.remove( existingKeyUNameIdx );
		dictByUNameIdx.put( newKeyUNameIdx, Buff );

		return(Buff);
	}

	public void deleteMimeType( ICFSecAuthorization Authorization,
		ICFIntMimeType Buff )
	{
		final String S_ProcName = "CFIntRamMimeTypeTable.deleteMimeType() ";
		String classCode;
		Integer pkey = schema.getFactoryMimeType().newPKey();
		pkey.setRequiredMimeTypeId( Buff.getRequiredMimeTypeId() );
		ICFIntMimeType existing = dictByPKey.get( pkey );
		if( existing == null ) {
			return;
		}
		if( existing.getRequiredRevision() != Buff.getRequiredRevision() )
		{
			throw new CFLibCollisionDetectedException( getClass(),
				"deleteMimeType",
				pkey );
		}
		CFIntBuffMimeTypeByUNameIdxKey keyUNameIdx = (CFIntBuffMimeTypeByUNameIdxKey)schema.getFactoryMimeType().newByUNameIdxKey();
		keyUNameIdx.setRequiredName( existing.getRequiredName() );

		// Validate reverse foreign keys

		// Delete is valid
		Map< Integer, CFIntBuffMimeType > subdict;

		dictByPKey.remove( pkey );

		dictByUNameIdx.remove( keyUNameIdx );

	}
	public void deleteMimeTypeByIdIdx( ICFSecAuthorization Authorization,
		Integer argKey )
	{
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		ICFIntMimeType cur;
		LinkedList<ICFIntMimeType> matchSet = new LinkedList<ICFIntMimeType>();
		Iterator<ICFIntMimeType> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<ICFIntMimeType> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableMimeType().readDerivedByIdIdx( Authorization,
				cur.getRequiredMimeTypeId() );
			deleteMimeType( Authorization, cur );
		}
	}

	public void deleteMimeTypeByUNameIdx( ICFSecAuthorization Authorization,
		String argName )
	{
		CFIntBuffMimeTypeByUNameIdxKey key = (CFIntBuffMimeTypeByUNameIdxKey)schema.getFactoryMimeType().newByUNameIdxKey();
		key.setRequiredName( argName );
		deleteMimeTypeByUNameIdx( Authorization, key );
	}

	public void deleteMimeTypeByUNameIdx( ICFSecAuthorization Authorization,
		ICFIntMimeTypeByUNameIdxKey argKey )
	{
		ICFIntMimeType cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<ICFIntMimeType> matchSet = new LinkedList<ICFIntMimeType>();
		Iterator<ICFIntMimeType> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<ICFIntMimeType> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableMimeType().readDerivedByIdIdx( Authorization,
				cur.getRequiredMimeTypeId() );
			deleteMimeType( Authorization, cur );
		}
	}
}
